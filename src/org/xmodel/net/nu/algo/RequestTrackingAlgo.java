package org.xmodel.net.nu.algo;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.xmodel.IModelObject;
import org.xmodel.log.Log;
import org.xmodel.net.nu.DefaultEventHandler;
import org.xmodel.net.nu.ITransport;
import org.xmodel.net.nu.ITransport.Error;
import org.xmodel.net.nu.ITransportImpl;
import org.xmodel.net.nu.protocol.IEnvelopeProtocol;
import org.xmodel.net.nu.protocol.IEnvelopeProtocol.Type;
import org.xmodel.xpath.expression.IContext;

/**
 * NOTE: Must be inserted before AbstractTransport in EventPipe.
 */
public class RequestTrackingAlgo extends DefaultEventHandler
{
  public RequestTrackingAlgo( ITransportImpl transport, ScheduledExecutorService scheduler)
  {
    this.transport = transport;
    this.requests = new ConcurrentHashMap<String, Request>();
    this.scheduler = scheduler;
    this.keyCounter = new AtomicLong( System.nanoTime());
    
    log.setLevel( Log.all);
  }
  
  @Override
  public boolean notifySend( IModelObject envelope, IContext messageContext, int timeout, int retries, int life)
  {
    IEnvelopeProtocol envelopeProtocol = transport.getProtocol().envelope(); 
    if ( envelopeProtocol.isRequest( envelope))
    {
      String key = Long.toString( keyCounter.getAndIncrement(), 36);
      envelopeProtocol.setKey( envelope, key);
      
      Request requestState = new Request( envelope, messageContext, timeout);
      requests.put( key, requestState);
    }
    
    return false;
  }

  @Override
  public boolean notifyReceive( IModelObject envelope)
  {
    if ( !transport.getProtocol().envelope().isRequest( envelope))
    {
      handleResponse( envelope);
      return true;
    }
    return false;
  }
  
  private void handleResponse( IModelObject envelope)
  {
    Object key = transport.getProtocol().envelope().getKey( envelope);
    if ( key != null)
    {
      Request request = requests.remove( key);
      
      // receive/timeout exclusion
      if ( request != null && request.timeoutFuture.cancel( false))
      {
        if ( transport.getProtocol().envelope().getType( envelope) != Type.ack)
          transport.getEventPipe().notifyReceive( envelope, request.messageContext, request.envelope);
      }
    }
  }

  @Override
  public boolean notifyDisconnect( IContext transportContext) throws IOException
  {
    //
    // Fail pending requests.  ConcurrentHashMap guarantees that all requests sent before
    // the iterator is created will be returned.  Subsequent requests should fail because
    // the channel is "inactive".
    //
    Iterator<Entry<String, Request>> iter = requests.entrySet().iterator();
    while( iter.hasNext())
    {
      Entry<String, Request> entry = iter.next();
      Request request = entry.getValue();
      if ( request.timeoutFuture.cancel( false))
      {
        iter.remove();
        transport.getEventPipe().notifyError( request.messageContext, ITransport.Error.channelClosed, request.envelope);
      }
    }
    
    return false;
  }

  @Override
  public boolean notifyError( IContext context, Error error, IModelObject envelope)
  {
    if ( envelope != null) requests.remove( transport.getProtocol().envelope().getKey( envelope));
    return false;
  }

  private void notifyTimeout( Request request, IModelObject envelope, IContext messageContext)
  {
    transport.getEventPipe().notifyError( messageContext, ITransport.Error.timeout, request.envelope);
  }

  protected class Request implements Runnable
  {
    Request( IModelObject envelope, IContext messageContext, int timeout)
    {
      this.envelope = envelope;
      this.messageContext = messageContext;
      this.timeout = timeout;
      
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
    int timeout;
  }

  public final static Log log = Log.getLog( RequestTrackingAlgo.class);
  
  private ITransportImpl transport;
  private ScheduledExecutorService scheduler;
  private Map<String, Request> requests;
  private AtomicLong keyCounter;
}
