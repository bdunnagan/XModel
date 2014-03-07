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
  public AmqpXioChannel( Connection connection, String exchangeName, String inQueue, String outQueue) throws IOException
  {
    this.connection = connection;
    this.inQueue = inQueue;
    this.outQueue = outQueue;
    this.exchangeName = exchangeName;
    this.threadChannels = new ThreadLocal<Channel>();
  }

  /**
   * Set the peer associated with this channel.
   * @param peer The peer.
   */
  protected void setPeer( XioPeer peer)
  {
    this.peer = peer;
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
   * Set the outgoing queue.
   * @param name The name of the queue.
   */
  public void setReplyQueue( String name)
  {
    outQueue = name;
  }
  
  /**
   * Create a new channel for the specified registration name.
   * @param name The name with which the endpoint registered.
   * @return Returns a new channel.
   */
  public AmqpXioChannel deriveRegisteredChannel( String name) throws IOException
  {
    return new AmqpXioChannel( connection, exchangeName, AmqpQueueNames.getRequestQueue( name), AmqpQueueNames.getResponseQueue( name));
  }
  
  /**
   * Start the consumer.
   * @param executor The i/o executor (used for heartbeat timeouts).
   * @param timeout The heartbeat timeout in milliseconds.
   */
  public void startConsumer( Executor executor, int timeout) throws IOException
  {
    Channel channel = getThreadChannel();
    
    if ( inQueue != null)
    {
      channel.queueDeclare( inQueue, false, false, true, null);
      channel.basicConsume( inQueue, true, inQueue, this);
    }
    
    if ( outQueue != null)
    {
      channel.queueDeclare( outQueue, false, false, true, null);
    }
    
    heartbeat = new Heartbeat( peer, timeout / 3, timeout, executor);
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
   * @see org.xmodel.net.IXioChannel#writeRequest(org.jboss.netty.buffer.ChannelBuffer)
   */
  @Override
  public void writeRequest( ChannelBuffer buffer)
  {
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
   * @see org.xmodel.net.IXioChannel#writeResponse(org.jboss.netty.buffer.ChannelBuffer)
   */
  @Override
  public void writeResponse( ChannelBuffer buffer)
  {
    try
    {
      byte[] bytes = new byte[ buffer.readableBytes()];
      buffer.readBytes( bytes);
      getThreadChannel().basicPublish( exchangeName, inQueue, null, bytes);
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
    log.debugf( "handleCancel: consumerTag=%s", consumerTag);
  }

  /* (non-Javadoc)
   * @see com.rabbitmq.client.Consumer#handleCancelOk(java.lang.String)
   */
  @Override
  public void handleCancelOk( String consumerTag)
  {
    log.debugf( "handleCancelOk: consumerTag=%s", consumerTag);
  }

  /* (non-Javadoc)
   * @see com.rabbitmq.client.Consumer#handleConsumeOk(java.lang.String)
   */
  @Override
  public void handleConsumeOk( String consumerTag)
  {
    log.debugf( "handleConsumeOk: consumerTag=%s", consumerTag);
  }

  /* (non-Javadoc)
   * @see com.rabbitmq.client.Consumer#handleDelivery(java.lang.String, com.rabbitmq.client.Envelope, com.rabbitmq.client.AMQP.BasicProperties, byte[])
   */
  @Override
  public void handleDelivery( String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException
  {
    log.debugf( "handleDelivery: consumerTag=%s", consumerTag);
    heartbeat.messageReceived();
    peer.handleMessage( this, ChannelBuffers.wrappedBuffer( body));
  }

  /* (non-Javadoc)
   * @see com.rabbitmq.client.Consumer#handleRecoverOk(java.lang.String)
   */
  @Override
  public void handleRecoverOk( String consumerTag)
  {
    log.debugf( "handleRecoverOk: consumerTag=%s", consumerTag);
  }

  /* (non-Javadoc)
   * @see com.rabbitmq.client.Consumer#handleShutdownSignal(java.lang.String, com.rabbitmq.client.ShutdownSignalException)
   */
  @Override
  public void handleShutdownSignal( String consumerTag, ShutdownSignalException signal)
  {
    log.debugf( "handleShutdownSignal: consumerTag=%s, signal=%s", consumerTag, signal);
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
  
  private static Log log = Log.getLog( AmqpXioChannel.class);
  
  private XioPeer peer;
  private String exchangeName;
  private String inQueue;
  private String outQueue;
  private Connection connection;
  private AsyncFuture<IXioChannel> closeFuture;
  private ThreadLocal<Channel> threadChannels;
  protected Heartbeat heartbeat;
}
