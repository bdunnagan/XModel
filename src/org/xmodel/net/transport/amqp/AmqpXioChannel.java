package org.xmodel.net.transport.amqp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Executor;
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
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

public class AmqpXioChannel implements IXioChannel, Consumer
{
  public AmqpXioChannel( Connection connection, String exchangeName, Executor executor) throws IOException
  {
    this.connection = connection;
    this.exchangeName = exchangeName;
    this.executor = executor;
    this.threadChannels = new ThreadLocal<Channel>();
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
   * Create a new channel on the same connection with the same parameters but no queues.
   * @return Returns a new channel.
   */
  public AmqpXioChannel deriveRegisteredChannel() throws IOException
  {
    return new AmqpXioChannel( connection, exchangeName, executor);
  }
  
  /**
   * Declare the output queue for this channel.
   * @param queue The name of the queue.
   * @param durable True if the queue is durable.
   * @param autoDelete True if the queue is auto-delete.
   */
  public void declareOutputQueue( String queue, boolean durable, boolean autoDelete) throws IOException
  {
    if ( queue == null || queue.length() == 0)
      throw new IllegalArgumentException( "Attempt to declare null/empty queue!");
    
    outQueue = queue;
    getThreadChannel().queueDeclare( outQueue, durable, false, autoDelete, null);
  }
  
  /**
   * Start the consumer.
   */
  public void startConsumer( String queue, boolean durable, boolean autoDelete) throws IOException
  {
    if ( queue == null || queue.length() == 0)
      throw new IllegalArgumentException( "Attempt to start consumer with null/empty queue!");
    
    inQueue = queue;
    Channel channel = getThreadChannel();
    channel.queueDeclare( inQueue, durable, false, autoDelete, null);
    channel.basicConsume( inQueue, true, inQueue+"[consumer]", this);
  }

  /**
   * Start a heartbeat schedule to detect remote connection loss.
   * @param timeout The timeout in milliseconds.
   * @param isClient True if heartbeat from the client.
   */
  public void startHeartbeat( int timeout, boolean isClient)
  {
    heartbeat = new Heartbeat( peer, timeout / 3, timeout, executor, isClient);
    heartbeat.start();
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
      log.debugf( "Writing %s message to %s queue", type, outQueue);
      buffer.resetReaderIndex();
    }
    
    try
    {
      byte[] bytes = new byte[ buffer.readableBytes()];
      buffer.readBytes( bytes);
      getThreadChannel().basicPublish( exchangeName, outQueue, null, bytes);
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
    AsyncFuture<IXioChannel> future = getCloseFuture();
    try
    {
      connection.close();
      future.notifySuccess();
    }
    catch( IOException e)
    {
      log.exception( e);
      future.notifyFailure( e);
    }
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
  
  /* (non-Javadoc)
   * @see com.rabbitmq.client.Consumer#handleCancel(java.lang.String)
   */
  @Override
  public void handleCancel( String consumerTag) throws IOException
  {
    log.debugf( "handleCancel: q=%s", this);
  }

  /* (non-Javadoc)
   * @see com.rabbitmq.client.Consumer#handleCancelOk(java.lang.String)
   */
  @Override
  public void handleCancelOk( String consumerTag)
  {
    log.debugf( "handleCancelOk: q=%s", this);
  }

  /* (non-Javadoc)
   * @see com.rabbitmq.client.Consumer#handleConsumeOk(java.lang.String)
   */
  @Override
  public void handleConsumeOk( String consumerTag)
  {
    log.debugf( "handleConsumeOk: q=%s", this);
  }

  /* (non-Javadoc)
   * @see com.rabbitmq.client.Consumer#handleDelivery(java.lang.String, com.rabbitmq.client.Envelope, com.rabbitmq.client.AMQP.BasicProperties, byte[])
   */
  @Override
  public void handleDelivery( String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException
  {
    log.debugf( "handleDelivery: q=%s", this);
    if ( heartbeat != null) heartbeat.messageReceived();
    peer.handleMessage( this, ChannelBuffers.wrappedBuffer( body));
  }

  /* (non-Javadoc)
   * @see com.rabbitmq.client.Consumer#handleRecoverOk(java.lang.String)
   */
  @Override
  public void handleRecoverOk( String consumerTag)
  {
    log.debugf( "handleRecoverOk: q=%s", this);
  }

  /* (non-Javadoc)
   * @see com.rabbitmq.client.Consumer#handleShutdownSignal(java.lang.String, com.rabbitmq.client.ShutdownSignalException)
   */
  @Override
  public void handleShutdownSignal( String consumerTag, ShutdownSignalException signal)
  {
    log.debugf( "handleShutdownSignal: q=%s, signal=%s", this, signal);
    if ( peer != null) peer.getPeerRegistry().unregisterAll( peer);
  }

  /**
   * @return Returns the channel for the current thread.
   */
  private Channel getThreadChannel() throws IOException
  {
    Channel channel = threadChannels.get();
    if ( channel == null)
    {
      channel = connection.createChannel();
      threadChannels.set( channel);
    }
    return channel;
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
    return inQueue+"<>"+outQueue;
  }

  private static Log log = Log.getLog( AmqpXioChannel.class);
  
  private AmqpXioPeer peer;
  private String exchangeName;
  private String inQueue;
  private String outQueue;
  private Connection connection;
  private AsyncFuture<IXioChannel> closeFuture;
  private Executor executor;
  private ThreadLocal<Channel> threadChannels;
  protected Heartbeat heartbeat;
}
