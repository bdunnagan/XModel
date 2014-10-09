package org.xmodel.net.nu;

import io.netty.buffer.Unpooled;
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
import org.xmodel.IModelObject;
import org.xmodel.future.AsyncFuture;
import org.xmodel.log.Log;
import org.xmodel.net.nu.protocol.IEnvelopeProtocol;
import org.xmodel.net.nu.protocol.Protocol;
import org.xmodel.net.nu.protocol.ThreadSafeProtocol;
import org.xmodel.util.HexDump;
import org.xmodel.util.PrefixThreadFactory;
import org.xmodel.xml.IXmlIO.Style;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.expression.IContext;

public abstract class AbstractTransport implements ITransportImpl, IEventHandler
{
  protected AbstractTransport( Protocol protocol, IContext transportContext, ScheduledExecutorService scheduler)
  {
    if ( scheduler == null) scheduler = Executors.newScheduledThreadPool( 1, new PrefixThreadFactory( "scheduler"));

    this.protocol = new ThreadSafeProtocol( protocol.wire(), protocol.envelope());
    this.transportContext = transportContext;
    this.scheduler = scheduler;
    this.requests = new ConcurrentHashMap<String, Request>();
    
    eventPipe = new EventPipe();
    eventPipe.addFirst( this);    
    
    log.setLevel( Log.all);
  }
  
  protected void setRouter( IRouter router)
  {
    this.router = router;
  }
  
  @Override
  public AsyncFuture<ITransport> sendRequest( IModelObject request, IContext messageContext, int timeout, int retries, int life)
  {
    String key = protocol.envelope().getKey( request);
    
    Request requestState = new Request( request, messageContext, timeout, retries);
    requests.put( key, requestState);
    
    eventPipe.notifySend( request, messageContext, timeout, retries, life);
    
    return sendImpl( request, null);
  }

  @Override
  public AsyncFuture<ITransport> sendAck( IModelObject request)
  {
    if ( request == null) throw new IllegalArgumentException();
    
    IEnvelopeProtocol envelopeProtocol = protocol.envelope();
    String key = envelopeProtocol.getKey( request);
    String route = envelopeProtocol.getRoute( request);
    
    IModelObject ack = envelopeProtocol.buildAck( key, route);
    return sendImpl( ack, request);
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.nu.ITransport#sendResponse(org.xmodel.IModelObject, org.xmodel.IModelObject)
   */
  @Override
  public AsyncFuture<ITransport> sendResponse( IModelObject response, IModelObject request)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public EventPipe getEventPipe()
  {
    return eventPipe;
  }

  protected ScheduledExecutorService getScheduler()
  {
    return scheduler;
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
  public boolean notifySend( IModelObject envelope, IContext messageContext, int timeout, int retries, int life)
  {
    return false;
  }

  @Override
  public boolean notifyReceive( ByteBuffer buffer) throws IOException
  {
    if ( log.verbose()) log.verbosef( "Read buffer contains:\n%s", HexDump.toString( Unpooled.wrappedBuffer( buffer)));
    
    try
    {
      // decode
      IModelObject envelope = protocol.wire().decode( buffer);
      if ( envelope != null)
      {
        // deliver
        eventPipe.notifyReceive( envelope);
        return true;
      }
    }
    catch( IOException e)
    {
      eventPipe.notifyException( e);
    }
    
    return false;
  }
  
  @Override
  public boolean notifyReceive( IModelObject envelope)
  {
    if ( log.verbose()) log.verbosef( "Transport, %s, received message, %s", this, XmlIO.write( Style.printable, envelope));
    
    switch( protocol.envelope().getType( envelope))
    {
      case register:   handleRegister( envelope); return true;
      case deregister: handleDeregister( envelope); return true;
      case request:    handleRequest( envelope); return true;
      case response:   
      case ack:        handleResponse( envelope); return true;
      default:          return false;
    }
  }
  
  private void handleRegister( IModelObject envelope)
  {
    IEnvelopeProtocol envelopeProtocol = protocol.envelope();
    String route = envelopeProtocol.getRoute( envelope);
    if ( route == null)
    {
      String name = envelopeProtocol.getRegistrationName( envelope);
      if ( router != null) 
      {
        router.addRoute( name, this);
      
        IModelObject ack = envelopeProtocol.buildAck( envelopeProtocol.getKey( envelope), route);
        sendImpl( ack, envelope);
        
        eventPipe.notifyRegister( transportContext, name);
      }
    }
    else
    {
      // TODO
      throw new UnsupportedOperationException();
    }
  }
  
  private void handleDeregister( IModelObject envelope)
  {
    IEnvelopeProtocol envelopeProtocol = protocol.envelope();
    String route = envelopeProtocol.getRoute( envelope);
    if ( route == null)
    {
      String name = envelopeProtocol.getRegistrationName( envelope);
      if ( router != null)
      {
        router.removeRoute( name, this);
      
        IModelObject ack = envelopeProtocol.buildAck( envelopeProtocol.getKey( envelope), route);
        sendImpl( ack, envelope);
        
        eventPipe.notifyRegister( transportContext, name);
      }
    }
    else
    {
      // TODO
      throw new UnsupportedOperationException();
    }
  }
  
  private void handleRequest( IModelObject envelope)
  {
    IEnvelopeProtocol envelopeProtocol = protocol.envelope();
    IModelObject message = envelopeProtocol.getMessage( envelope);
    String route = envelopeProtocol.getRoute( envelope);
    if ( route == null)
    {
      eventPipe.notifyReceive( message, transportContext, null);
    }
    else
    {
      // TODO
      throw new UnsupportedOperationException();
    }
  }
  
  private void handleResponse( IModelObject envelope)
  {
    IEnvelopeProtocol envelopeProtocol = protocol.envelope();
    IModelObject message = envelopeProtocol.getMessage( envelope);
    Object key = envelopeProtocol.getKey( envelope);
    if ( key != null)
    {
      Request request = requests.remove( key);
      
      // receive/timeout exclusion
      if ( request != null && request.timeoutFuture.cancel( false))
      {
        IModelObject requestMessage = envelopeProtocol.getMessage( request.envelope);
        eventPipe.notifyReceive( message, request.messageContext, requestMessage);
      }
    }
  }
  
  @Override
  public boolean notifyReceive( IModelObject message, IContext messageContext, IModelObject requestMessage)
  {
    return false;
  }

  @Override
  public boolean notifyConnect(IContext transportContext) throws IOException
  {
    return false;
  }

  @Override
  public boolean notifyDisconnect(IContext transportContext) throws IOException
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
        eventPipe.notifyError( request.messageContext, ITransport.Error.channelClosed, requestMessage);
      }
    }
  }
  
  @Override
  public boolean notifyRegister( IContext transportContext, String name)
  {
    return false;
  }

  @Override
  public boolean notifyDeregister( IContext transportContext, String name)
  {
    return false;
  }

  @Override
  public boolean notifyError( IContext context, Error error, IModelObject request)
  {
    if ( request != null)
    {
      // free request resources
      IEnvelopeProtocol envelopeProtocol = protocol.envelope();
      IModelObject envelope = envelopeProtocol.getEnvelope( request);
      requests.remove( envelopeProtocol.getKey( envelope));
    }
    
    return false;
  }

  @Override
  public boolean notifyException( IOException e)
  {
    return false;
  }
  
  private void notifyTimeout( Request request, IModelObject envelope, IContext messageContext)
  {
    if ( --request.retries >= 0)
    {
      sendImpl( envelope, null);
    }
    else
    {
      IModelObject requestMessage = protocol.envelope().getMessage( envelope);        
      eventPipe.notifyError( messageContext, ITransport.Error.timeout, requestMessage);
    }
  }

  protected class Request implements Runnable
  {
    Request( IModelObject envelope, IContext messageContext, int timeout, int retries)
    {
      this.envelope = envelope;
      this.messageContext = messageContext;
      this.retries = retries;
      
      if ( timeout > 0)
      {
        this.timeoutFuture = scheduler.schedule( this, timeout, TimeUnit.MILLISECONDS);
      }
    }
    
    @Override
    public void run()
    {
      notifyTimeout( this, envelope, messageContext);
    }
    
    IModelObject envelope;
    IContext messageContext;
    ScheduledFuture<?> timeoutFuture;
    int retries;
  }
    
  public final static Log log = Log.getLog( AbstractTransport.class);
 
  private IRouter router;
  private Protocol protocol;
  private IContext transportContext;
  private ScheduledExecutorService scheduler;
  private Map<String, Request> requests;
  private EventPipe eventPipe;
}