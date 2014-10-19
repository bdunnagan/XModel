package org.xmodel.net.nu.algo;

import java.io.IOException;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.xmodel.IModelObject;
import org.xmodel.log.Log;
import org.xmodel.net.nu.DefaultEventHandler;
import org.xmodel.net.nu.ITransport;
import org.xmodel.net.nu.ITransport.Error;
import org.xmodel.net.nu.ITransportImpl;
import org.xmodel.xpath.expression.IContext;

public class ReliableAlgo extends DefaultEventHandler
{
  public ReliableAlgo( ITransportImpl transport, ScheduledExecutorService scheduler)
  {
    this.transport = transport;
    this.scheduler = scheduler;
    this.resendTolerance = 100;
    
    // TODO: overkill?
    this.queue = new ConcurrentLinkedQueue<QueuedMessage>();
    this.sent = new ConcurrentHashMap<IModelObject, QueuedMessage>();
    
    log.setLevel( Log.all);
  }
  
  public void setResendTolerance( int tolerance)
  {
    resendTolerance = tolerance;
  }
  
  @Override
  public boolean notifySend( ITransportImpl transport, IModelObject envelope, IContext messageContext, int timeout, int retries, int life)
  {
    if ( transport.getProtocol().envelope().isRequest( envelope))
    {
      QueuedMessage item = sent.get( envelope);
      if ( item == null && (retries >= 0 || life >= 0))
      {
        long expiry = (life >= 0)? System.currentTimeMillis() + life: -1;
        item = new QueuedMessage( transport, envelope, messageContext, timeout, retries, expiry);
        sent.put( envelope, item);
      }
    }

    return false;
  }

  @Override
  public boolean notifyReceive( ITransportImpl transport, IModelObject message, IContext messageContext, IModelObject request)
  {
    if ( request != null) sent.remove( request);
    return false;
  }

  @Override
  public boolean notifyConnect(ITransportImpl transport, IContext transportContext) throws IOException
  {
    sendNextFromBacklog();
    return false;
  }

  @Override
  public boolean notifyError( ITransportImpl transport, IContext context, Error error, IModelObject request)
  {
    if ( request != null)
    {
      if ( error.equals( ITransport.Error.timeout))
      {
        //
        // Remote host was busy, so re-send message unless it has expired.
        // If this behavior is not desired, then set the expiry of the message to
        // be <= timeout.
        //
        QueuedMessage item = sent.remove( request);
        if ( item != null)
        {
          int timeRemaining = item.getTimeRemaining();
          if ( timeRemaining > resendTolerance) 
          {
            log.debugf( "Message timeout, %s: retries=%d, expiry=%d", request, item.retries, timeRemaining);
            
            if ( item.retries == Integer.MAX_VALUE || item.retries-- > 0)
            {
              transport.send( null, item.message, item.messageContext, Math.min( timeRemaining, item.timeout), item.retries, timeRemaining);
              sent.put( request, item);
              return true;
            }
          }
          else
          {
            notifyExpired( item);
            return true;
          }
        }
      }
      else
      {
        //
        // Connection is offline, so queue message until connection is re-established.
        //
        QueuedMessage item = sent.remove( request);
        if ( item != null) 
        {
          putMessageInBacklog( item);
          return true;
        }
        else
        {
          return false;
        }
      }
    }
    
    return false;
  }

  private void putMessageInBacklog( QueuedMessage item)
  {
    log.debugf( "Queueing message, %s", item.message);
    
    int timeRemaining = item.getTimeRemaining();
    if ( timeRemaining > resendTolerance)
    {
      item.expireFuture = scheduler.schedule( new ExpireTask( item), timeRemaining, TimeUnit.MILLISECONDS);
      queue.offer( item);
    }
    else
    {
      notifyExpired( item);
    }
  }
  
  private void sendNextFromBacklog()
  {
    QueuedMessage item = queue.poll();
    while( item != null)
    {
      if ( item.expireFuture.cancel( false))
      {
        int timeRemaining = item.getTimeRemaining();
        if ( timeRemaining > resendTolerance) 
        {
          transport.send( null, item.message, item.messageContext, Math.min( timeRemaining, item.timeout), item.retries, timeRemaining);
        }
        else
        {
          notifyExpired( item);
        }
      }
      
      item = queue.poll();
    }
  }
  
  private void notifyExpired( QueuedMessage item)
  {
    transport.getEventPipe().notifyError( item.transport, item.messageContext, ITransport.Error.messageExpired, item.message);
  }    
  
  private class QueuedMessage
  {
    public QueuedMessage( ITransportImpl transport, IModelObject message, IContext messageContext, int timeout, int retries, long expiry)
    {
      this.transport = transport;
      this.message = message;
      this.messageContext = messageContext;
      this.timeout = timeout;
      this.retries = (retries != 0)? retries: Integer.MAX_VALUE;
      this.expiry = expiry;
    }
    
    public int getTimeRemaining()
    {
      return (expiry >= 0)? (int)(expiry - System.currentTimeMillis()): Integer.MAX_VALUE;
    }
    
    public ITransportImpl transport;
    public IModelObject message;
    public IContext messageContext;
    public int timeout;
    public int retries;
    public long expiry;
    public ScheduledFuture<?> expireFuture;
  }
  
  private class ExpireTask implements Runnable
  {
    public ExpireTask( QueuedMessage item)
    {
      this.item = item;
    }
    
    @Override
    public void run()
    {
      notifyExpired( item);
    }

    private QueuedMessage item;
  }
  
  public final static Log log = Log.getLog( ReliableAlgo.class);
  
  private ITransportImpl transport;
  private ScheduledExecutorService scheduler;
  private Queue<QueuedMessage> queue;
  private Map<IModelObject, QueuedMessage> sent;
  private int resendTolerance;
}
