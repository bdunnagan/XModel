package org.xmodel.net.connection.amqp;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import org.jboss.netty.buffer.ChannelBuffers;
import org.xmodel.future.AsyncFuture;
import org.xmodel.future.SuccessAsyncFuture;
import org.xmodel.log.SLog;
import org.xmodel.net.XioPeer;
import org.xmodel.net.HeaderProtocol.Type;
import org.xmodel.net.connection.AbstractNetworkConnection;
import org.xmodel.net.connection.INetworkConnection;
import org.xmodel.net.connection.INetworkMessage;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Address;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * Implementation of INetworkConnection to connect to RabbitMQ.
 */
public class AmqpNetworkConnection extends AbstractNetworkConnection
{
  public AmqpNetworkConnection( ConnectionFactory connectionFactory, Address[] brokers, ExecutorService executor)
  {
    this.exchange = "";
    this.outQueue = "";
    this.connectionFactory = connectionFactory;
    this.brokers = brokers;
    this.executor = executor;
    this.threadChannels = new ThreadLocal<Channel>();
  }
  
  /**
   * @return Returns an AMQP Channel for declaring queues and exchanges.
   */
  public Channel getAmqpChannel() throws IOException
  {
    return getThreadChannel();
  }
  
  /**
   * Set the output exchange to use when sending messages.
   * @param exchange The exchange.
   */
  public void setOutputExchange( String exchange)
  {
    this.exchange = exchange;
  }
  
  /**
   * Set the output queue to use when sending messages.
   * @param queue The queue.
   */
  public void setOutputQueue( String queue)
  {
    this.outQueue = queue;
  }
  
  /**
   * Set the queue from which messages will be consumed.
   * @param queue The queue.
   */
  public void setInputQueue( String queue)
  {
    this.inQueue = queue;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.connection.INetworkConnection#connect()
   */
  @Override
  public synchronized AsyncFuture<INetworkConnection> connect()
  {
    AsyncFuture<INetworkConnection> connectFuture = new AsyncFuture<INetworkConnection>( this);
    
    if ( connection != null && connection.isOpen())
    {
      connectFuture.notifySuccess();
      return connectFuture;
    }

    closeFuture = new AsyncFuture<INetworkConnection>( this);
    
    try
    {
      startConsumer();
    }
    catch( Exception e)
    {
      connectFuture.notifyFailure( e);
      return connectFuture;
    }
    
    try
    {
      connection = connectionFactory.newConnection( executor, brokers);
      connectFuture.notifySuccess();
    }
    catch( Exception e)
    {
      connectFuture.notifyFailure( e);
      
      try
      {
        stopConsumer();
      }
      catch( Exception e2)
      {
        SLog.exception( this, e2);
      }
    }
    
    return connectFuture;
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.connection.INetworkConnection#closeImpl()
   */
  @Override
  public synchronized AsyncFuture<INetworkConnection> closeImpl()
  {
    if ( closeFuture == null) return new SuccessAsyncFuture<INetworkConnection>( this);
    
    if ( connection == null || !connection.isOpen())
    {
      closeFuture.notifySuccess();
      return closeFuture;
    }
    
    try
    {
      connection.close();
      closeFuture.notifySuccess();
    }
    catch( Exception e)
    {
      closeFuture.notifyFailure( e);
    }
    
    return closeFuture;
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.connection.INetworkConnection#getCloseFuture()
   */
  @Override
  public AsyncFuture<INetworkConnection> getCloseFuture()
  {
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.connection.INetworkConnection#send(org.xmodel.net.connection.INetworkMessage)
   */
  @Override
  public void send( INetworkMessage message) throws IOException
  {
    if ( exchange.length() == 0 && outQueue.length() == 0)
      throw new IOException( "AmqpNetworkChannel does not define an exchange or a queue.");
    
    BasicProperties properties = null;
    if ( message instanceof AmqpNetworkMessage)
    {
      AmqpNetworkMessage amqpMessage = (AmqpNetworkMessage)message;
      properties = amqpMessage.getBasicProperties();
    }
    
    byte[] bytes = message.getBytes();
    getThreadChannel().basicPublish( exchange, outQueue, properties, bytes);
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.connection.AbstractNetworkConnection#handleListenerException(java.lang.Exception)
   */
  @Override
  protected void handleListenerException( Exception e)
  {
    SLog.exception( this, e);
  }
  
  /**
   * Start the AMQP consumer.
   */
  private void startConsumer() throws IOException
  {
    try
    {
      consumerChannel = connection.createChannel();
      consumerChannel.basicConsume( inQueue, new AmqpConsumer( consumerChannel));
    }
    catch( Exception e)
    {
      try
      {
        if ( consumerChannel != null) consumerChannel.close();
      }
      catch( Exception e2)
      {
        SLog.exception( this, e2);
      }
      
      throw new IOException( "Failed trying to start AMQP consumer...", e);
    }
  }
  
  /**
   * Stop the AMQP consumer.
   */
  private void stopConsumer() throws IOException
  {
    if ( consumerChannel != null) consumerChannel.close();
  }

  /**
   * @return Returns the channel for the current thread.
   */
  private synchronized Channel getThreadChannel() throws IOException
  {
    if ( connection == null) throw new IOException( "AmqpNetworkConnection is not connected.");
    
    Channel channel = threadChannels.get();
    if ( channel == null)
    {
      channel = connection.createChannel();
      threadChannels.set( channel);
    }
    return channel;
  }
  
  private class AmqpConsumer extends DefaultConsumer
  {
    public AmqpConsumer( Channel channel)
    {
      super( channel);
    }

    /* (non-Javadoc)
     * @see com.rabbitmq.client.DefaultConsumer#handleDelivery(java.lang.String, com.rabbitmq.client.Envelope, com.rabbitmq.client.AMQP.BasicProperties, byte[])
     */
    @Override
    public void handleDelivery( String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException
    {
      if ( threadChannels.get() != getChannel()) threadChannels.set( getChannel());

      // dispatch through original protocol classes for now
      Type type = headerProtocol.readType( buffer);
      log.debugf( "Message Type: %s", type);
      
      long length = headerProtocol.readLength( buffer);
      if ( buffer.readableBytes() < length) return false;
      log.debugf( "Message Length: %d", length);
      
      switch( type)
      {
        case echoRequest:     echoProtocol.requestProtocol.handle( channel, buffer); return true;
        case echoResponse:    echoProtocol.responseProtocol.handle( channel, buffer); return true;
        
        case executeRequest:  executionProtocol.requestProtocol.handle( channel, buffer, headerProtocol.readCorrelation( buffer)); return true;
        case cancelRequest:   executionProtocol.requestProtocol.handleCancel( channel, buffer); return true;
        case executeResponse: executionProtocol.responseProtocol.handle( channel, buffer); return true;
        
        case bindRequest:     bindProtocol.bindRequestProtocol.handle( channel, buffer, length); return true;
        case bindResponse:    bindProtocol.bindResponseProtocol.handle( channel, buffer, length); return true;
        case unbindRequest:   bindProtocol.unbindRequestProtocol.handle( channel, buffer); return true;
        case syncRequest:     bindProtocol.syncRequestProtocol.handle( channel, buffer); return true;
        case syncResponse:    bindProtocol.syncResponseProtocol.handle( channel, buffer); return true;
        case addChild:        bindProtocol.updateProtocol.handleAddChild( channel, buffer); return true;
        case removeChild:     bindProtocol.updateProtocol.handleRemoveChild( channel, buffer); return true;
        case changeAttribute: bindProtocol.updateProtocol.handleChangeAttribute( channel, buffer); return true;
        case clearAttribute:  bindProtocol.updateProtocol.handleClearAttribute( channel, buffer); return true;
        case changeDirty:     bindProtocol.updateProtocol.handleChangeDirty( channel, buffer); return true;
        
        case register:        registerProtocol.registerRequestProtocol.handle( channel, buffer); return true;
        case unregister:      registerProtocol.unregisterRequestProtocol.handle( channel, buffer); return true;
      }
      
      
      //onMessageReceived( new AmqpNetworkMessage( properties, body));
    }

    /* (non-Javadoc)
     * @see com.rabbitmq.client.DefaultConsumer#handleCancelOk(java.lang.String)
     */
    @Override
    public void handleCancelOk( String consumerTag)
    {
      if ( threadChannels.get() != getChannel()) threadChannels.set( getChannel());
      onClose( closedFutureMessage);
    }

    /* (non-Javadoc)
     * @see com.rabbitmq.client.DefaultConsumer#handleShutdownSignal(java.lang.String, com.rabbitmq.client.ShutdownSignalException)
     */
    @Override
    public void handleShutdownSignal( String consumerTag, ShutdownSignalException signal)
    {
      if ( threadChannels.get() != getChannel()) threadChannels.set( getChannel());
      // TODO: translate signal into common exceptions and messages
      onClose( signal);
    }
  }
  
  private String exchange;
  private String outQueue;
  private String inQueue;
  private ConnectionFactory connectionFactory;
  private Connection connection;
  private Address[] brokers;
  private ExecutorService executor;
  private ThreadLocal<Channel> threadChannels;
  private Channel consumerChannel;
  private AsyncFuture<INetworkConnection> closeFuture;
  private XioPeer peer;
}
