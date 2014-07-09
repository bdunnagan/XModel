package org.xmodel.net.nu;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;

import org.xmodel.IModelObject;
import org.xmodel.future.AsyncFuture;
import org.xmodel.net.nu.protocol.Protocol;
import org.xmodel.xpath.expression.IContext;

public class ReliableTransport implements ITransportImpl, IEventHandler
{
  public ReliableTransport( ITransportImpl transport)
  {
    this.transport = transport;
    transport.getEventPipe().addLast( this);
    
    // TODO: overkill?
    this.queue = new ConcurrentLinkedQueue<QueuedMessage>();
    this.sent = new ConcurrentHashMap<IModelObject, QueuedMessage>();
  }
  
  @Override
  public AsyncFuture<ITransport> connect( int timeout)
  {
    return transport.connect( timeout);
  }

  @Override
  public AsyncFuture<ITransport> disconnect()
  {
    return transport.disconnect();
  }

  public AsyncFuture<ITransport> request( IModelObject message, IContext messageContext, int timeout, int life)
  {
    long expiry = System.currentTimeMillis() + life;
    
    QueuedMessage item = new QueuedMessage( message, messageContext, timeout, expiry);
    sent.put( message, item);
    
    return transport.request( message, messageContext, timeout);
  }
  
  @Override
  public AsyncFuture<ITransport> request( IModelObject message, IContext messageContext, int timeout)
  {
    return request( message, messageContext, timeout, timeout);
  }
  
  @Override
  public AsyncFuture<ITransport> ack( IModelObject request)
  {
    return transport.ack( request);
  }

  @Override
  public AsyncFuture<ITransport> respond( IModelObject message, IModelObject request)
  {
    return transport.respond( message, request);
  }

  @Override
  public ScheduledFuture<?> schedule( Runnable runnable, int delay)
  {
    return transport.schedule( runnable, delay);
  }

  @Override
  public AsyncFuture<ITransport> sendImpl( IModelObject envelope)
  {
    return transport.sendImpl( envelope);
  }

  @Override
  public Protocol getProtocol()
  {
    return transport.getProtocol();
  }

  @Override
  public IContext getTransportContext()
  {
    return transport.getTransportContext();
  }

  @Override
  public EventPipe getEventPipe()
  {
    return transport.getEventPipe();
  }

  @Override
  public boolean notifyReceive( ByteBuffer buffer) throws IOException
  {
    return false;
  }

  @Override
  public boolean notifyReceive( IModelObject message, IContext messageContext, IModelObject request)
  {
    if ( request != null) sent.remove( request);
    return false;
  }

  @Override
  public boolean notifyConnect() throws IOException
  {
    sendNextFromBacklog();
    return false;
  }

  @Override
  public boolean notifyDisconnect() throws IOException
  {
    return false;
  }

  @Override
  public boolean notifyError( IContext context, Error error, IModelObject request)
  {
    if ( request != null)
    {
      if ( error.equals( ITransport.Error.timeout))
      {
        //
        // Remote host was busy, so re-send message unless it has expired.
        //
        QueuedMessage item = sent.get( request);
        if ( item != null)
        {
          int timeRemaining = (int)(item.expiry - System.currentTimeMillis());
          if ( timeRemaining > 0) 
          {
            request( item.message, item.messageContext, Math.min( timeRemaining, item.timeout), timeRemaining);
          }
          else
          {
            getEventPipe().notifyError( item.messageContext, ITransport.Error.messageExpired, item.message);
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
        }
        else
        {
          getEventPipe().notifyError( context, error, request);
        }
      }
    }
    else
    {
      getEventPipe().notifyError( context, error, request);
    }    
    
    return false;
  }

  @Override
  public boolean notifyException( IOException e)
  {
    return false;
  }

  private void putMessageInBacklog( QueuedMessage item)
  {
    int timeRemaining = (int)(item.expiry - System.currentTimeMillis());
    if ( timeRemaining > 0)
    {
      item.expireFuture = transport.schedule( new ExpireTask( item), timeRemaining);
      queue.offer( item);
    }
    else
    {
      getEventPipe().notifyError( item.messageContext, ITransport.Error.messageExpired, item.message);
    }
  }
  
  private void sendNextFromBacklog()
  {
    QueuedMessage item = queue.poll();
    if ( item != null)
    {
      if ( item.expireFuture.cancel( false))
      {
        int timeRemaining = (int)(item.expiry - System.currentTimeMillis());
        if ( timeRemaining > 0) 
        {
          AsyncFuture<ITransport> writeFuture = request( item.message, item.messageContext, Math.min( timeRemaining, item.timeout));
          writeFuture.addListener( writeListener);
        }
      }
    }
  }
  
  private class QueuedMessage
  {
    public QueuedMessage( IModelObject message, IContext messageContext, int timeout, long expiry)
    {
      this.message = message;
      this.messageContext = messageContext;
      this.timeout = timeout;
      this.expiry = expiry;
    }
    
    public IModelObject message;
    public IContext messageContext;
    public int timeout;
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
      getEventPipe().notifyError( item.messageContext, ITransport.Error.messageExpired, item.message);
    }

    private QueuedMessage item;
  }
  
  private AsyncFuture.IListener<ITransport> writeListener = new AsyncFuture.IListener<ITransport>() {
    @Override
    public void notifyComplete( AsyncFuture<ITransport> future) throws Exception
    {
      sendNextFromBacklog();
    }
  };
  
  private ITransportImpl transport;
  private Queue<QueuedMessage> queue;
  private Map<IModelObject, QueuedMessage> sent;
}
