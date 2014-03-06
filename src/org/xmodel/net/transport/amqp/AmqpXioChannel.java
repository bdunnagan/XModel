package org.xmodel.net.transport.amqp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
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
  public AmqpXioChannel( Connection connection, String exchangeName, String requestQueueName, String responseQueueName)
  {
    this.connection = connection;
    this.requestQueueName = requestQueueName;
    this.responseQueueName = responseQueueName;
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
      getThreadChannel().basicPublish( exchangeName, requestQueueName, null, bytes);
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
      getThreadChannel().basicPublish( exchangeName, responseQueueName, null, bytes);
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
  
  /**
   * Create a non-durable, auto-delete in-bound queue by which the client will receive requests.
   * Create a non-durable, auto-delete out-bound queue by appending "-reply" to the end of the
   * client subscription name.
   * Create a consumer on the in-bound queue.
   * @param name The name by which the client is registering.
   */
  public void createServiceQueues( String name) throws IOException
  {
    Channel channel = getThreadChannel();
    
    // in-bound/requests
    requestQueueName = name;
    channel.queueDeclare( requestQueueName, false, false, false, null);
    channel.basicConsume( requestQueueName, true, "inbound", this);
    
    // out-bound/responses
    responseQueueName = name+"-reply"; 
    channel.queueDeclare( responseQueueName, false, false, false, null);
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
  
  private static Log log = Log.getLog( AmqpClientTransport.class);
  
  private XioPeer peer;
  private String exchangeName;
  private String requestQueueName;
  private String responseQueueName;
  private Connection connection;
  private AsyncFuture<IXioChannel> closeFuture;
  private ThreadLocal<Channel> threadChannels;
}
