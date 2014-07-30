package org.xmodel.net.nu.amqp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import org.xmodel.IModelObject;
import org.xmodel.future.AsyncFuture;
import org.xmodel.future.FailureAsyncFuture;
import org.xmodel.future.SuccessAsyncFuture;
import org.xmodel.log.SLog;
import org.xmodel.net.nu.AbstractTransport;
import org.xmodel.net.nu.ITransport;
import org.xmodel.net.nu.protocol.Protocol;
import org.xmodel.xpath.expression.IContext;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

public class AbstractAmqpTransport extends AbstractTransport
{
  public static final String notConnectedError = "Not connected";

  public AbstractAmqpTransport( Protocol protocol, IContext transportContext, ScheduledExecutorService scheduler)
  {
    super( protocol, transportContext, scheduler);
  
    exchange = "";
    queue = "xio";
    
    connectionFactory = new ConnectionFactory();
    connectionFactory.setHost( "localhost");
  }
  
  public void setRemoteAddress( String host)
  {
    connectionFactory.setHost( host);
  }
  
  public void setExchange( String exchange)
  {
    this.exchange = exchange;
  }
  
  public void setQueue( String queue)
  {
    this.queue = queue;
  }
  
  @Override
  public AsyncFuture<ITransport> sendImpl( IModelObject envelope)
  {
    Channel channel = sendChannelRef.get();
    if ( channel == null) return new FailureAsyncFuture<ITransport>( this, notConnectedError); 
  
    final AsyncFuture<ITransport> future = new AsyncFuture<ITransport>( this) {
      @Override 
      public void cancel()
      {
        throw new UnsupportedOperationException();
      }
    };
    
    // encode
    byte[] bytes = encode( envelope, future);
    if ( bytes != null)
    {
      try
      {
        // publish
        channel.basicPublish( exchange, queue, null, bytes);
      }
      catch( IOException e)
      {
        future.notifyFailure( e);
      }
    }
    
    return future;
  }

  @Override
  public AsyncFuture<ITransport> connect( int timeout)
  {
    Channel channel = sendChannelRef.get();
    if ( channel != null && channel.isOpen()) 
      return new SuccessAsyncFuture<ITransport>( this);
    
    try
    {
      connectionFactory.setConnectionTimeout( timeout);
      connection = connectionFactory.newConnection(); // use connection pool, or define connection separately
      sendChannelRef.set( connection.createChannel());
      return new SuccessAsyncFuture<ITransport>( this);
    }
    catch( IOException e)
    {
      return new FailureAsyncFuture<ITransport>( this, e);
    }
  }

  @Override
  public AsyncFuture<ITransport> disconnect()
  {
    try
    {
      sendChannelRef.set( null);
      connection.close();
    }
    catch( IOException e)
    {
      SLog.exception( this, e);
    }

    connection = null;
    
    return new SuccessAsyncFuture<ITransport>( this);
  }
  
  private byte[] encode( IModelObject envelope, AsyncFuture<ITransport> future)
  {
    try
    {
      return getProtocol().wire().encode( envelope);
    }
    catch( IOException e)
    {
      future.notifyFailure( e);
      getEventPipe().notifyError( getTransportContext(), ITransport.Error.encodeFailed, null);
      return null;
    }
  }
  
  private class TransportConsumer extends DefaultConsumer
  {
    public TransportConsumer( Channel channel)
    {
      super( channel);
    }
    
    @Override
    public void handleDelivery( String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException
    {
      getEventPipe().notifyReceive( ByteBuffer.wrap( body));
    }

    @Override
    public void handleShutdownSignal( String consumerTag, ShutdownSignalException signal)
    {
      Object reason = signal.getReason();
      if ( !(reason instanceof AMQP.Channel.Close) || ((AMQP.Channel.Close)reason).getReplyCode() != 200)
      {
        SLog.exceptionf( this, signal, "Unhandled exception ...");
      }
      
      try
      {
        getEventPipe().notifyDisconnect( getTransportContext());
      }
      catch( IOException e)
      {
        SLog.exception( this, e);
      }
    }
  };
  
  private ConnectionFactory connectionFactory;
  private Connection connection;
  private String exchange;
  private String queue;
  private AtomicReference<Channel> sendChannelRef;
}
