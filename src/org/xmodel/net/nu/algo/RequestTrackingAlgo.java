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
import org.xmodel.xpath.expression.IContext;

/**
 * NOTE: Must be inserted before AbstractTransport in EventPipe.
 */
public class RequestTrackingAlgo extends DefaultEventHandler
{
  public RequestTrackingAlgo( ScheduledExecutorService scheduler)
  {
    this.requests = new ConcurrentHashMap<Long, Request>();
    this.scheduler = scheduler;
    this.keyCounter = new AtomicLong( System.nanoTime());
    
    log.setLevel( Log.all);
  }
  
  private Long getKey( ITransportImpl transport, IModelObject envelope)
  {
    Object key = transport.getProtocol().envelope().getKey( envelope);
    if ( key != null)
    {
      if ( key instanceof Number) return ((Number)key).longValue();
      else if ( key instanceof String) return Long.parseLong( key.toString());
    }
    return null;
  }
  
  @Override
  public boolean notifySend( ITransportImpl transport, IModelObject envelope, IContext messageContext, int timeout, int retries, int life)
  {
    IEnvelopeProtocol envelopeProtocol = transport.getProtocol().envelope(); 
    if ( envelopeProtocol.isRequest( envelope))
    {
      Long key = keyCounter.getAndIncrement();
      envelopeProtocol.setKey( envelope, key);
      
      Request requestState = new Request( transport, envelope, messageContext, timeout);
      requests.put( key, requestState);
    }
    
    return false;
  }

  @Override
  public boolean notifyReceive( ITransportImpl transport, IModelObject envelope)
  {
    if ( !transport.getProtocol().envelope().isRequest( envelope))
    {
      handleResponse( transport, envelope);
      return true;
    }
    return false;
  }
  
  private void handleResponse( ITransportImpl transport, IModelObject envelope)
  {
    Long key = getKey( transport, envelope);
    if ( key != null)
    {
      Request request = requests.remove( key);
      
      // receive/timeout exclusion
      if ( request != null && (request.timeoutFuture == null || request.timeoutFuture.cancel( false)))
      {
        transport.getEventPipe().notifyReceive( transport, envelope, request.messageContext, request.envelope);
      }
    }
  }

  @Override
  public boolean notifyDisconnect( ITransportImpl transport, IContext transportContext) throws IOException
  {
    //
    // Fail pending requests.  ConcurrentHashMap guarantees that all requests sent before
    // the iterator is created will be returned.  Subsequent requests should fail because
    // the channel is "inactive".
    //
    Iterator<Entry<Long, Request>> iter = requests.entrySet().iterator();
    while( iter.hasNext())
    {
      Entry<Long, Request> entry = iter.next();
      Request request = entry.getValue();
      if ( request.timeoutFuture == null || request.timeoutFuture.cancel( false))
      {
        iter.remove();
        transport.getEventPipe().notifyError( transport, request.messageContext, ITransport.Error.channelClosed, request.envelope);
      }
    }
    
    return false;
  }

  @Override
  public boolean notifyError( ITransportImpl transport, IContext context, Error error, IModelObject request)
  {
    if ( request != null) 
    {
      Long key = getKey( transport, request);
      if ( key != null) requests.remove( key);
    }
    return false;
  }

  private void notifyTimeout( ITransportImpl transport, Request request, IModelObject envelope, IContext messageContext)
  {
    transport.getEventPipe().notifyError( transport, messageContext, ITransport.Error.timeout, request.envelope);
  }
  
  protected class Request implements Runnable
  {
    Request( ITransportImpl transport, IModelObject envelope, IContext messageContext, int timeout)
    {
      this.transport = transport;
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
      notifyTimeout( transport, this, envelope, messageContext);
    }
    
    public ITransportImpl transport;
    public IModelObject envelope;
    public IContext messageContext;
    public ScheduledFuture<?> timeoutFuture;
    public int timeout;
  }

  public final static Log log = Log.getLog( RequestTrackingAlgo.class);
  
  private ScheduledExecutorService scheduler;
  private Map<Long, Request> requests;
  private AtomicLong keyCounter;
}
