package org.xmodel.net.connection;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.xmodel.future.AsyncFuture;
import org.xmodel.log.SLog;

/**
 * An implementation of INetworkConnection that queues sent messages for a period of time 
 * defined by the lifetime parameter of the message.  If a message cannot be positively
 * delivered within the specified lifetime, then the sender is notified that the message
 * could not be delivered. 
 * TODO: this code is incorrect and not finished yet
 */
public class QueuedConnection extends AbstractNetworkConnection
{
  public interface IListener extends INetworkConnection.IListener
  {
    /**
     * Called when a message expires before it can be sent.
     * @param connection The connection.
     * @param message The message.
     */
    public void onMessageExpired( INetworkConnection connection, INetworkMessage message);
  }
  
  public QueuedConnection( INetworkConnectionFactory connectionFactory, Queue<INetworkMessage> queue, ScheduledExecutorService scheduler)
  {
    super();
    
    this.connectionFactory = connectionFactory;
    this.queue = queue;
    this.scheduler = scheduler;
    this.reconnectFutureRef = new AtomicReference<ScheduledFuture<?>>();
  }
  
  /**
   * Set the retry delay for send failures.
   * @param delay The delay in milliseconds.
   */
  public void setRetryDelay( int delay)
  {
    this.retryDelay = delay;
  }
  
  /**
   * Send the specified message and return a future for the send operation.
   * @param message The message to send.
   * @return Returns the future for the operation.
   */
  public AsyncFuture<INetworkMessage> queueSend( INetworkMessage message)
  {
    AsyncFuture<INetworkMessage> future = new AsyncFuture<INetworkMessage>( message);
    
    if ( !queue.offer( message))
    {
      future.notifyFailure( queueFullMessage);
      return future;
    }

    dequeue();
    
    return future;
  }
  
  /**
   * Dequeue and attempt to send next message.
   */
  private void dequeue()
  {
    try
    {
      while( true)
      {
        INetworkMessage message = queue.peek();
        if ( message == null) break;
        
        long expires = message.getExpiration();
        if ( expires > 0)
        {
          long life = expires - System.currentTimeMillis();
          if ( life < 0)
          {
            handleExpiredMessage( message);
            continue;
          }
        }
        
        connection.send( message);
        queue.poll();
      }
    }
    catch( Exception e)
    {
      SLog.debugf( this, "Send failed because, %s", e.toString());

      ScheduledFuture<?> reconnectFuture = reconnectFutureRef.get();
      if ( reconnectFuture != null && reconnectFuture.cancel( false))
      {
        reconnectFutureRef.set( scheduler.schedule( reconnectTask, retryDelay, TimeUnit.MILLISECONDS));
      }
    }
  }

  /**
   * Attempt to reconnect and send next message from queue.
   */
  private synchronized void reconnect()
  {
    connection.close();
    connect().addListener( connectListener);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.connection.INetworkConnection#connect()
   */
  @Override
  public synchronized AsyncFuture<INetworkConnection> connect()
  {
    if ( connection == null) connection = connectionFactory.newConnection();
    return connection.connect();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.connection.INetworkConnection#send(java.lang.Object)
   */
  @Override
  public void send( Object message) throws IOException
  {
    connection.send( message);
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.connection.AbstractNetworkConnection#closeImpl()
   */
  @Override
  protected AsyncFuture<INetworkConnection> closeImpl()
  {
    return connection.close();
  }
  
  /**
   * Notify listeners that a message expired.
   * @param message The message.
   */
  protected void handleExpiredMessage( INetworkMessage message)
  {
    for( INetworkConnection.IListener listener: getListeners())
    {
      try
      {
        if ( listener instanceof IListener)
        {
          ((IListener)listener).onMessageExpired( this, message);
        }
      }
      catch( Exception e)
      {
        try
        {
          handleListenerException( e);
        }
        catch( Exception e2)
        {
          SLog.exception( this, e2);
        }
      }
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.connection.AbstractNetworkConnection#handleListenerException(java.lang.Exception)
   */
  @Override
  protected void handleListenerException( Exception e)
  {
    SLog.exception( this, e);
  }

  private Runnable reconnectTask = new Runnable() {
    public void run()
    {
      reconnect();
    }
  };
  
  private AsyncFuture.IListener<INetworkConnection> connectListener = new AsyncFuture.IListener<INetworkConnection>() {
    public void notifyComplete( AsyncFuture<INetworkConnection> future) throws Exception
    {
      if ( future.isSuccess())
      {
        dequeue();
      }
      else
      {
        reconnectFutureRef.set( scheduler.schedule( reconnectTask, retryDelay, TimeUnit.MILLISECONDS));
      }
    }
  };
  
  private INetworkConnectionFactory connectionFactory;
  private INetworkConnection connection;
  private Queue<INetworkMessage> queue;
  private ScheduledExecutorService scheduler;
  private AtomicReference<ScheduledFuture<?>> reconnectFutureRef;
  private int retryDelay;
}
