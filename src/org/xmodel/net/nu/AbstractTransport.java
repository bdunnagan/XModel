package org.xmodel.net.nu;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.xmodel.IModelObject;
import org.xmodel.future.AsyncFuture;
import org.xmodel.log.Log;
import org.xmodel.net.nu.protocol.IEnvelopeProtocol;
import org.xmodel.net.nu.protocol.Protocol;
import org.xmodel.net.nu.protocol.ThreadSafeProtocol;
import org.xmodel.util.PrefixThreadFactory;
import org.xmodel.xpath.expression.IContext;

public abstract class AbstractTransport implements ITransport
{
  protected AbstractTransport( Protocol protocol, IContext transportContext, ScheduledExecutorService scheduler, 
     List<IConnectListener> connectListeners, List<IDisconnectListener> disconnectListeners,
     List<IReceiveListener> receiveListeners, List<IErrorListener> errorListeners)
  {
    if ( scheduler == null) scheduler = Executors.newScheduledThreadPool( 1, new PrefixThreadFactory( "scheduler"));
    if ( connectListeners == null) connectListeners = Collections.emptyList(); 
    if ( disconnectListeners == null) disconnectListeners = Collections.emptyList(); 
    if ( receiveListeners == null) receiveListeners = Collections.emptyList(); 
    if ( errorListeners == null) errorListeners = Collections.emptyList(); 
    
    this.protocol = new ThreadSafeProtocol( protocol.wire(), protocol.envelope());
    this.transportContext = transportContext;
    this.scheduler = scheduler;
    this.requests = new ConcurrentHashMap<String, Request>();
    this.requestCounter = new AtomicLong( System.nanoTime() & 0x7FFFFFFFFFFFFFFFL);
    
    this.connectListeners = new CopyOnWriteArrayList<IConnectListener>( connectListeners);
    this.disconnectListeners = new CopyOnWriteArrayList<IDisconnectListener>( disconnectListeners);
    this.receiveListeners = new CopyOnWriteArrayList<IReceiveListener>( receiveListeners);
    this.errorListeners = new CopyOnWriteArrayList<IErrorListener>( errorListeners);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.nu.ITransport#send(org.xmodel.IModelObject)
   */
  @Override
  public AsyncFuture<ITransport> send( IModelObject message)
  {
    IEnvelopeProtocol envelopeProtocol = protocol.envelope();
    IModelObject envelope = envelopeProtocol.buildEnvelope( null, null, message);
    return sendImpl( envelope);
  }

  @Override
  public final AsyncFuture<ITransport> request( IModelObject message, IContext messageContext, int timeout)
  {
    String key = Long.toHexString( requestCounter.incrementAndGet());
    IModelObject envelope = protocol.envelope().buildEnvelope( key, null, message);
    
    Request request = new Request( envelope, messageContext, timeout);
    requests.put( key, request);
    
    return sendImpl( envelope);
  }
    
  @Override
  public final AsyncFuture<ITransport> respond( IModelObject message, IModelObject request)
  {
    IEnvelopeProtocol envelopeProtocol = protocol.envelope();
    
    String key = null;
    String route = null;
    
    //
    // The envelope of the original request message is retrieved and its correlation key
    // extracted using the IEnvelopeProtocol.  This means that the IEnvelopeProtocol
    // must be able to find the envelope given the message body.
    //
    if ( request != null)
    {
      IModelObject envelope = envelopeProtocol.getEnvelope( request);
      key = envelopeProtocol.getKey( envelope);
      route = envelopeProtocol.getRoute( envelope);
    }
    
    IModelObject envelope = envelopeProtocol.buildEnvelope( key, route, message);
    return sendImpl( envelope);
  }
  
  protected abstract AsyncFuture<ITransport> sendImpl( IModelObject envelope);

  @Override
  public void addListener( IConnectListener listener)
  {
    if ( !connectListeners.contains( listener))
      connectListeners.add( listener);
  }

  @Override
  public void removeListener( IConnectListener listener)
  {
    connectListeners.remove( listener);
  }

  @Override
  public void addListener( IDisconnectListener listener)
  {
    if ( !disconnectListeners.contains( listener))
      disconnectListeners.add( listener);
  }

  @Override
  public void removeListener( IDisconnectListener listener)
  {
    disconnectListeners.remove( listener);
  }
  
  @Override
  public void addListener( IReceiveListener listener)
  {
    if ( !receiveListeners.contains( listener))
      receiveListeners.add( listener);
  }

  @Override
  public void removeListener( IReceiveListener listener)
  {
    receiveListeners.remove( listener);
  }

  @Override
  public void addListener( IErrorListener listener)
  {
    if ( !errorListeners.contains( listener))
      errorListeners.add( listener);
  }

  @Override
  public void removeListener( IErrorListener listener)
  {
    errorListeners.remove( listener);
  }

  public boolean notifyReceive( byte[] bytes, int offset, int length) throws IOException
  {
    try
    {
      // decode
      IModelObject envelope = protocol.wire().decode( bytes, offset, length);
      if ( envelope == null) return false;
      
      // deliver
      return notifyReceive( envelope);
    }
    catch( Exception e)
    {
      log.exception( e);
      return false;
    }
  }
  
  public boolean notifyReceive( ByteBuffer buffer) throws IOException
  {
    try
    {
      // decode
      IModelObject envelope = protocol.wire().decode( buffer);
      if ( envelope == null) return false;
      
      // deliver
      return notifyReceive( envelope);
    }
    catch( Exception e)
    {
      log.exception( e);
      return false;
    }
  }
  
  private boolean notifyReceive( IModelObject envelope) throws IOException
  {
    IEnvelopeProtocol envelopeProtocol = protocol.envelope();
    
    // get body
    IModelObject message = envelopeProtocol.getMessage( envelope);
    
    // get route
    String route = envelopeProtocol.getRoute( envelope);
    
    // lookup request and free
    Object key = envelopeProtocol.getKey( envelope);
    Request request = (key != null)? requests.remove( key): null;
    if ( request != null)
    {
      // receive/timeout exclusion
      if ( request.timeoutFuture.cancel( false))
      {
        for( IReceiveListener listener: receiveListeners)
        {
          try
          {
            listener.onReceive( this, message, request.messageContext, request.envelope);
          }
          catch( Exception e)
          {
            log.exception( e);
          }
        }
      }
    }
    else if ( route == null)
    {
      for( IReceiveListener listener: receiveListeners)
      {
        try
        {
          listener.onReceive( this, message, transportContext, null);
        }
        catch( Exception e)
        {
          log.exception( e);
        }
      }
    }
    else
    {
      // TODO
      throw new UnsupportedOperationException();
    }
    
    return true;
  }
  
  public void notifyError( IContext context, ITransport.Error error)
  {
    // notify listeners
    for( IErrorListener listener: errorListeners)
    {
      try
      {
        listener.onError( this, context, error);
      }
      catch( Exception e)
      {
        log.exception( e);
      }
    }
  }
  
  public void notifyConnect()
  {
    // notify listeners
    for( IConnectListener listener: connectListeners)
    {
      try
      {
        listener.onConnect( this, transportContext);
      }
      catch( Exception e)
      {
        log.exception( e);
      }
    }
  }
  
  public void notifyDisconnect()
  {
    // notify listeners
    for( IDisconnectListener listener: disconnectListeners)
    {
      try
      {
        listener.onDisconnect( this, transportContext);
      }
      catch( Exception e)
      {
        log.exception( e);
      }
    }
  }
  
  private void notifyTimeout( IModelObject envelope, IContext messageContext)
  {
    IEnvelopeProtocol envelopeProtocol = protocol.envelope();
    String key = envelopeProtocol.getKey( envelope);
    
    // release request
    requests.remove( key);
    
    notifyError( messageContext, ITransport.Error.timeout);
  }

  protected Protocol getProtocol()
  {
    return protocol;
  }
  
  protected IContext getTransportContext()
  {
    return transportContext;
  }
  
  private class Request implements Runnable
  {
    Request( IModelObject envelope, IContext messageContext, int timeout)
    {
      this.envelope = envelope;
      this.messageContext = messageContext;
      this.timeoutFuture = scheduler.schedule( this, timeout, TimeUnit.MILLISECONDS);
    }
    
    @Override
    public void run()
    {
      notifyTimeout( envelope, messageContext);
    }
    
    IModelObject envelope;
    IContext messageContext;
    ScheduledFuture<?> timeoutFuture;
  }
  
  public final static Log log = Log.getLog( AbstractTransport.class);
  
  private Protocol protocol;
  private IContext transportContext;
  private ScheduledExecutorService scheduler;
  private Map<String, Request> requests;
  private AtomicLong requestCounter;
  private List<IConnectListener> connectListeners;
  private List<IDisconnectListener> disconnectListeners;
  private List<IReceiveListener> receiveListeners;
  private List<IErrorListener> errorListeners;
}
