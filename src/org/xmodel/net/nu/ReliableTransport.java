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

public class ReliableTransport implements ITransportImpl, IConnectListener, IDisconnectListener, IReceiveListener, IErrorListener
{
  public ReliableTransport( ITransportImpl transport)
  {
    this.transport = transport;
    this.notifier = new TransportNotifier();
    
    transport.addListener( (IReceiveListener)this);
    transport.addListener( (IErrorListener)this);
    
    // TODO: overkill?
    this.queue = new ConcurrentLinkedQueue<QueuedMessage>();
    this.sent = new ConcurrentHashMap<IModelObject, QueuedMessage>();
  }
  
  @Override
  public void onConnect( ITransport transport, IContext context) throws Exception
  {
    sendNextFromBacklog();
    
    notifier.notifyConnect( this, context);
  }

  @Override
  public void onDisconnect( ITransport transport, IContext context) throws Exception
  {
    notifier.notifyDisconnect( this, context);
  }

  @Override
  public void onReceive( ITransport transport, IModelObject message, IContext messageContext, IModelObject request) throws Exception
  {
    if ( request != null) sent.remove( request);
    notifier.notifyReceive( this, message, messageContext, request);
  }

  @Override
  public void onError( ITransport transport, IContext context, Error error, IModelObject request) throws Exception
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
            notifier.notifyError( this, item.messageContext, ITransport.Error.messageExpired, item.message);
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
          notifier.notifyError( this, context, error, request);
        }
      }
    }
    else
    {
      notifier.notifyError( this, context, error, request);
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
  public boolean notifyReceive( byte[] bytes, int offset, int length) throws IOException
  {
    return transport.notifyReceive( bytes, offset, length);
  }

  @Override
  public boolean notifyReceive( ByteBuffer buffer) throws IOException
  {
    return transport.notifyReceive( buffer);
  }

  @Override
  public void notifyConnect() throws IOException
  {
    transport.notifyConnect();
  }

  @Override
  public void notifyDisconnect() throws IOException
  {
    transport.notifyDisconnect();
  }

  @Override
  public void notifyError( IContext context, Error error, IModelObject request) throws Exception
  {
    transport.notifyError( context, error, request);
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
      notifier.notifyError( this, item.messageContext, ITransport.Error.messageExpired, item.message);
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
      notifier.notifyError( ReliableTransport.this, item.messageContext, ITransport.Error.messageExpired, item.message);
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
  private TransportNotifier notifier;
  private Queue<QueuedMessage> queue;
  private Map<IModelObject, QueuedMessage> sent;
}
