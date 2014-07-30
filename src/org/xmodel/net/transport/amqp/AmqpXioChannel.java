package org.xmodel.net.transport.amqp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.ssl.SslHandler;
import org.xmodel.future.AsyncFuture;
import org.xmodel.log.Log;
import org.xmodel.net.HeaderProtocol;
import org.xmodel.net.HeaderProtocol.Type;
import org.xmodel.net.IXioChannel;
import org.xmodel.net.XioPeer;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;

public class AmqpXioChannel implements IXioChannel
{
  public AmqpXioChannel( AutoRefreshConnection connection, String exchangeName, Executor executor, int timeout) throws IOException
  {
    this.connection = connection;
    this.exchangeName = exchangeName;
    this.executor = executor;
    this.timeout = timeout;
    this.timeoutScheduleRef = new AtomicReference<ScheduledFuture<?>>();
    this.closed = new AtomicReference<Boolean>( false);
  }

  /**
   * Set the peer associated with this channel.
   * @param peer The peer.
   */
  protected void setPeer( XioPeer peer)
  {
    this.peer = (AmqpXioPeer)peer;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.IXioChannel#getPeer()
   */
  @Override
  public XioPeer getPeer()
  {
    return peer;
  }

  /**
   * @return Returns the amqp Connection instance.
   */
  public AutoRefreshConnection getConnection()
  {
    return connection;
  }
  
  /**
   * Create a new channel on the same connection with the same parameters but no queues.
   * @return Returns a new channel.
   */
  public AmqpXioChannel deriveRegisteredChannel() throws IOException
  {
    return new AmqpXioChannel( connection, "", executor, timeout);
  }
  
  /**
   * Declare the output queue for this channel.  The queue is purged of records.
   * @param queue The name of the queue.
   * @param durable Declare queue durable.
   * @param autoDelete Declare queue auto-delete.
   */
  public void setOutputQueue( String queue, boolean durable, boolean autoDelete) throws IOException
  {
    if ( queue == null || queue.length() == 0)
      throw new IllegalArgumentException( "Attempt to declare null/empty queue!");
    
    outQueue = queue;
    
    Channel channel = connection.leaseChannel();
    try
    {
      channel.queueDeclare( outQueue, durable, false, autoDelete, null);
    }
    finally
    {
      connection.returnChannel( channel);
    }
  }
  
  /**
   * Create the heartbeat fanout exchange and start sending heartbeat messages.
   */
  public void startHeartbeat() throws IOException
  {
    this.heartbeatPeriod = (int)(timeout / 2.5f);
    heartbeatTimer = scheduler.schedule( heartbeatTask, heartbeatPeriod, TimeUnit.MILLISECONDS);
  }

  /**
   * Start the heartbeat message expiration timer.
   */
  public AsyncFuture<AmqpXioPeer> startHeartbeatTimeout()
  {
    timeoutFuture = new AsyncFuture<AmqpXioPeer>( peer);
    timeoutScheduleRef.set( scheduler.schedule( timeoutTask, timeout, TimeUnit.MILLISECONDS));
    log.verbosef( "\n\nStart timer: channel=%X, future=%X\n", hashCode(), timeoutFuture.hashCode());
    return timeoutFuture;
  }
  
  /**
   * Start the consumer.
   * @param queue The name of the queue.
   * @param durable Declare queue as durable.
   * @param autoDelete Declare queue as auto-delete.
   * @param purge True if input queue should be purged before consumer is started.
   */
  public void startConsumer( String queue, boolean durable, boolean autoDelete, boolean purge) throws IOException
  {
    if ( queue == null || queue.length() == 0)
      throw new IllegalArgumentException( "Attempt to start consumer with null/empty queue!");

    inQueue = queue;
    connection.startConsumer( queue, durable, autoDelete, purge, this);
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.IXioChannel#isConnected()
   */
  @Override
  public boolean isConnected()
  {
    return connection.isOpen();
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.IXioChannel#write(org.jboss.netty.buffer.ChannelBuffer)
   */
  @Override
  public void write( ChannelBuffer buffer)
  {
    if ( outQueue == null) throw new IllegalStateException( "Output queue not defined.");
    
    if ( log.debug())
    {
      buffer.markReaderIndex();
      HeaderProtocol header = new HeaderProtocol();
      Type type = header.readType( buffer);
      log.debugf( "Writing %s message to e='%s',q='%s'", type, exchangeName, outQueue);
      buffer.resetReaderIndex();
    }
    
    try
    {
      byte[] bytes = new byte[ buffer.readableBytes()];
      buffer.readBytes( bytes);
      
      Channel channel = connection.leaseChannel();
      try
      {
        channel.basicPublish( exchangeName, outQueue, null, bytes);
      }
      finally
      {
        connection.returnChannel( channel);
      }
    }
    catch( IOException e)
    {
      log.exception( e);
      // TODO:
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.IXioChannel#close()
   */
  @Override
  public AsyncFuture<IXioChannel> close()
  {
    closed.set( true);
    
    // cancel timeout
    ScheduledFuture<?> timeoutFuture = timeoutScheduleRef.get();
    if ( timeoutFuture != null) timeoutFuture.cancel( false);
    
    // cancel heartbeat
    if ( heartbeatTimer != null) heartbeatTimer.cancel( false);
    
    // clear registry
    getPeer().getPeerRegistry().unregisterAll( getPeer());
    
    // stop consumers
    AsyncFuture<IXioChannel> future = getCloseFuture();
    try
    {
//      if ( heartbeatConsumerChannel != null) heartbeatConsumerChannel.close();
      connection.stopConsumer( this);
    }
    catch( IOException e)
    {
      log.exception( e);
      future.notifyFailure( e);
    }
    
    future.notifySuccess();
    return future;
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.IXioChannel#getSslHandler()
   */
  @Override
  public SslHandler getSslHandler()
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.IXioChannel#getLocalAddress()
   */
  @Override
  public SocketAddress getLocalAddress()
  {
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.IXioChannel#getRemoteAddress()
   */
  @Override
  public SocketAddress getRemoteAddress()
  {
    InetAddress address = connection.getAddress();
    int port = connection.getPort();
    return new InetSocketAddress( address, port);
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.IXioChannel#getCloseFuture()
   */
  @Override
  public synchronized AsyncFuture<IXioChannel> getCloseFuture()
  {
    if ( closeFuture == null) closeFuture = new AsyncFuture<IXioChannel>( this);
    return closeFuture;
  }
  
  /**
   * @return Returns null or the input queue name.
   */
  protected String inQueue()
  {
    return inQueue;
  }
  
  /**
   * @return Returns null or the output queue name.
   */
  protected String outQueue()
  {
    return outQueue;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    return inQueue+" <-> "+outQueue;
  }

  /**
   * Consume a message.
   * @param channel The consumer channel.
   * @param consumerTag 
   * @param envelope
   * @param properties
   * @param body
   */
  public void handleDelivery( Channel channel, String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException
  {
    channel.basicAck( envelope.getDeliveryTag(), false);
    
    try
    {
      log.debugf( "handleDelivery: e=%s, q=%s", envelope.getExchange(), envelope.getRoutingKey());
      
      if ( closed.get())
      {
        log.warnf( "Delivery aborted on closed channel: e=%s, q=%s", envelope.getExchange(), envelope.getRoutingKey());
        return;
      }
      
      ScheduledFuture<?> timer = timeoutScheduleRef.get();
      if ( timer != null && timer.cancel( false))
      {
        timeoutScheduleRef.set( scheduler.schedule( timeoutTask, timeout, TimeUnit.MILLISECONDS));
        log.verbosef( "\n\nRefreshing timeout: channel=%X, consumer=%X, oldFuture=%X, newFuture=%X\n", AmqpXioChannel.this.hashCode(), hashCode(), timer.hashCode(), timeoutScheduleRef.get().hashCode());
      }
      
      if ( heartbeatTimer != null)
      {
        heartbeatTimer.cancel( false);        
        heartbeatTimer = scheduler.schedule( heartbeatTask, heartbeatPeriod, TimeUnit.MILLISECONDS);
      }
      
      peer.handleMessage( AmqpXioChannel.this, ChannelBuffers.wrappedBuffer( body));
    }
    catch( Exception e)
    {
      log.errorf( "Caught exception handling message from queue, %s", envelope.getRoutingKey());
      log.exception( e);
    }
  }
  
  private Runnable heartbeatTask = new Runnable() {
    public void run()
    {
      try 
      { 
        peer.heartbeat( AmqpXioChannel.this);
      } 
      catch( Exception e) 
      {
        log.error( "Unable to send heartbeat...", e);
      }      
    }
  };
  
  private Runnable timeoutTask = new Runnable() {
    public void run()
    {
      log.verbosef( "\n\nDispatch Timeout: channel=%X\n", AmqpXioChannel.this.hashCode());

      timeoutScheduleRef.set( null);
      
      executor.execute( new Runnable() {
        public void run()
        {
          timeoutFuture.notifySuccess();
        }
      });
    }
  };
  
  private static Log log = Log.getLog( AmqpXioChannel.class);
  private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool( 1);
  
  private AmqpXioPeer peer;
  private String exchangeName;
  private String inQueue;
  private String outQueue;
  private AutoRefreshConnection connection;
  private AsyncFuture<IXioChannel> closeFuture;
  private Executor executor;
  private int heartbeatPeriod;
  private int timeout;
  private AtomicReference<ScheduledFuture<?>> timeoutScheduleRef;
  private ScheduledFuture<?> heartbeatTimer;
  private AsyncFuture<AmqpXioPeer> timeoutFuture;
  private AtomicReference<Boolean> closed;
}
