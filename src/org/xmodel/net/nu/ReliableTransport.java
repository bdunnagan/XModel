package org.xmodel.net.nu;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ScheduledFuture;
import org.xmodel.IModelObject;
import org.xmodel.future.AsyncFuture;
import org.xmodel.net.nu.protocol.Protocol;
import org.xmodel.xpath.expression.IContext;

public class ReliableTransport implements ITransportImpl
{
  public ReliableTransport( ITransportImpl transport)
  {
    this.transport = transport;
  }
  
  @Override
  public void onConnect( ITransport transport, IContext context) throws Exception
  {
    sendNextFromBacklog();
  }

  @Override
  public void onReceive( ITransport transport, IModelObject message, IContext messageContext, IModelObject request) throws Exception
  {
    if ( request != null) sent.remove( request);
  }
  
  @Override
  public void onError( ITransport transport, IContext context, Error error, IModelObject request) throws Exception
  {
    // ignore expiration errors
    if ( error.equals( ITransport.Error.messageExpired)) return;
    
    if ( request != null)
    {
      if ( error.equals( ITransport.Error.timeout))
      {
        //
        // Remote host was busy, so re-send message unless it has expired.
        //
        QueuedMessage item = sent.get( request);
        int timeRemaining = (int)(item.expiry - System.currentTimeMillis());
        if ( timeRemaining > 0) 
        {
          request( item.message, item.messageContext, Math.min( timeRemaining, item.timeout));
        }
        else
        {
          this.transport.notifyError( item.messageContext, ITransport.Error.messageExpired, item.message);
        }
      }
      else
      {
        //
        // Connection is offline, so queue message until connection is re-established.
        //
        QueuedMessage item = sent.remove( request);
        if ( item != null) putMessageInBacklog( item);
      }
    }
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
  public void addListener( IConnectListener listener)
  {
    transport.addListener( listener);
  }

  @Override
  public void removeListener( IConnectListener listener)
  {
    transport.removeListener( listener);
  }

  @Override
  public void addListener( IDisconnectListener listener)
  {
    transport.addListener( listener);
  }

  @Override
  public void removeListener( IDisconnectListener listener)
  {
    transport.removeListener( listener);
  }

  @Override
  public void addListener( IReceiveListener listener)
  {
    transport.addListener( listener);
  }

  @Override
  public void removeListener( IReceiveListener listener)
  {
    transport.removeListener( listener);
  }

  @Override
  public void addListener( IErrorListener listener)
  {
    transport.addListener( listener);
  }

  @Override
  public void removeListener( IErrorListener listener)
  {
    transport.removeListener( listener);
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
      transport.notifyError( item.messageContext, ITransport.Error.messageExpired, item.message);
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
      transport.notifyError( item.messageContext, ITransport.Error.messageExpired, item.message);
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
