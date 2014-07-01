package org.xmodel.net.nu;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
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

public abstract class AbstractTransport implements ITransportImpl
{
  protected AbstractTransport( Protocol protocol, IContext transportContext, ScheduledExecutorService scheduler)
  {
    if ( scheduler == null) scheduler = Executors.newScheduledThreadPool( 1, new PrefixThreadFactory( "scheduler"));

    this.protocol = new ThreadSafeProtocol( protocol.wire(), protocol.envelope());
    this.transportContext = transportContext;
    this.scheduler = scheduler;
    this.requests = new ConcurrentHashMap<String, Request>();
    this.requestCounter = new AtomicLong( System.nanoTime() & 0x7FFFFFFFFFFFFFFFL);
    this.notifier = new TransportNotifier();
  }

  @Override
  public final AsyncFuture<ITransport> request( IModelObject message, IContext messageContext, int timeout)
  {
    String key = Long.toHexString( requestCounter.incrementAndGet());
    IModelObject envelope = protocol.envelope().buildRequestEnvelope( key, null, message);
    
    Request request = new Request( envelope, messageContext, timeout);
    requests.put( key, request);
    
    return sendImpl( envelope);
  }
    
  @Override
  public AsyncFuture<ITransport> ack( IModelObject request)
  {
    if ( request == null) throw new IllegalArgumentException();
    
    IEnvelopeProtocol envelopeProtocol = protocol.envelope();
    
    IModelObject envelope = envelopeProtocol.getEnvelope( request);
    String key = envelopeProtocol.getKey( envelope);
    String route = envelopeProtocol.getRoute( envelope);
    
    IModelObject ack = envelopeProtocol.buildAck( key, route);
    return sendImpl( ack);
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
    
    IModelObject envelope = envelopeProtocol.buildResponseEnvelope( key, route, message);
    return sendImpl( envelope);
  }
  
  @Override
  public void addListener( IConnectListener listener)
  {
    notifier.addListener( listener);
  }

  @Override
  public void removeListener( IConnectListener listener)
  {
    notifier.removeListener( listener);
  }

  @Override
  public void addListener( IDisconnectListener listener)
  {
    notifier.addListener( listener);
  }

  @Override
  public void removeListener( IDisconnectListener listener)
  {
    notifier.removeListener( listener);
  }
  
  @Override
  public void addListener( IReceiveListener listener)
  {
    notifier.addListener( listener);
  }

  @Override
  public void removeListener( IReceiveListener listener)
  {
    notifier.removeListener( listener);
  }

  @Override
  public void addListener( IErrorListener listener)
  {
    notifier.addListener( listener);
  }

  @Override
  public void removeListener( IErrorListener listener)
  {
    notifier.removeListener( listener);
  }

  @Override
  public ScheduledFuture<?> schedule( Runnable runnable, int delay)
  {
    return scheduler.schedule( runnable, delay, TimeUnit.MILLISECONDS);
  }

  @Override
  public Protocol getProtocol()
  {
    return protocol;
  }
  
  @Override
  public IContext getTransportContext()
  {
    return transportContext;
  }

  @Override
  public boolean notifyReceive( ByteBuffer buffer) throws IOException
  {
    try
    {
      // decode
      IModelObject envelope = protocol.wire().decode( buffer);
      if ( envelope == null) return false;
      
      // deliver
      notifyReceive( envelope);
    }
    catch( Exception e)
    {
      log.exception( e);
    }
    
    return true;
  }
  
  private void notifyReceive( IModelObject envelope) throws IOException
  {
    IEnvelopeProtocol envelopeProtocol = protocol.envelope();
    
    IModelObject message = envelopeProtocol.getMessage( envelope);
    String route = envelopeProtocol.getRoute( envelope);
    if ( !envelopeProtocol.isRequest( envelope))
    {
      Object key = envelopeProtocol.getKey( envelope);
      if ( key != null)
      {
        Request request = requests.remove( key);
        
        // receive/timeout exclusion
        if ( request != null && request.timeoutFuture.cancel( false))
        {
          IModelObject requestMessage = envelopeProtocol.getMessage( request.envelope);
          notifier.notifyReceive( this, message, request.messageContext, requestMessage);
        }
      }
    }
    else if ( route == null)
    {
      notifier.notifyReceive( this, message, transportContext, null);
    }
    else
    {
      // TODO
      throw new UnsupportedOperationException();
    }
  }
  
  @Override
  public boolean notifyError( IContext context, Error error, IModelObject request) throws Exception
  {
    return false;
  }

  @Override
  public boolean notifyConnect() throws IOException
  {
    return false;
  }

  @Override
  public boolean notifyDisconnect() throws IOException
  {
    failPendingRequests();
    return false;
  }

  public void failPendingRequests()
  {
    //
    // Fail pending requests.  ConcurrentHashMap guarantees that all requests sent before
    // the iterator is created will be returned.  Subsequent requests should fail because
    // the channel is "inactive".
    //
    IEnvelopeProtocol envelopeProtocol = getProtocol().envelope();
    
    Iterator<Entry<String, Request>> iter = requests.entrySet().iterator();
    while( iter.hasNext())
    {
      Entry<String, Request> entry = iter.next();
      Request request = entry.getValue();
      if ( request.timeoutFuture.cancel( false))
      {
        iter.remove();
        IModelObject requestMessage = envelopeProtocol.getMessage( request.envelope);        
        notifyError( request.messageContext, ITransport.Error.channelClosed, requestMessage);
      }
    }
  }
  
  private void notifyTimeout( IModelObject envelope, IContext messageContext)
  {
    IEnvelopeProtocol envelopeProtocol = protocol.envelope();
    String key = envelopeProtocol.getKey( envelope);
    
    // release request
    requests.remove( key);
    
    IModelObject requestMessage = envelopeProtocol.getMessage( envelope);        
    notifyError( messageContext, ITransport.Error.timeout, requestMessage);
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
}