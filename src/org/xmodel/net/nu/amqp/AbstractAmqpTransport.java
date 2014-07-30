package org.xmodel.net.nu.amqp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import org.xmodel.IModelObject;
import org.xmodel.future.AsyncFuture;
import org.xmodel.future.FailureAsyncFuture;
import org.xmodel.future.SuccessAsyncFuture;
import org.xmodel.log.SLog;
import org.xmodel.net.nu.AbstractTransport;
import org.xmodel.net.nu.IRouter;
import org.xmodel.net.nu.ITransport;
import org.xmodel.net.nu.SimpleRouter;
import org.xmodel.net.nu.protocol.Protocol;
import org.xmodel.xpath.expression.IContext;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.Queue.DeclareOk;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

public class AbstractAmqpTransport extends AbstractTransport implements IRouter
{
  public static final String notConnectedError = "Not connected";

  public AbstractAmqpTransport( Protocol protocol, IContext transportContext, ScheduledExecutorService scheduler)
  {
    super( protocol, transportContext, scheduler);
  
    publishExchange = "";
    publishQueue = "xio";
    
    connectionFactory = new ConnectionFactory();
    connectionFactory.setHost( "localhost");
    
    router = new SimpleRouter();
    setRouter( this);
  }
  
  public void setRemoteAddress( String host)
  {
    connectionFactory.setHost( host);
  }
  
  public void setPublishExchange( String exchange)
  {
    this.publishExchange = exchange;
  }
  
  public void setPublishQueue( String queue)
  {
    this.publishQueue = queue;
  }
  
  public void setConsumeQueue( String queue)
  {
    this.consumeQueue = queue;
  }
  
  @Override
  public AsyncFuture<ITransport> register( String name, IContext messageContext, int timeout)
  {
    AsyncFuture<ITransport> future = super.register( name, messageContext, timeout);
    
    // close consumer
    // undeclare consumeQueue
    // set consumeQueue = name
    // declare consumeQueue
    
    return future;
  }

  @Override
  public AsyncFuture<ITransport> sendImpl( IModelObject envelope)
  {
    Channel channel = publishChannelRef.get();
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
        BasicProperties properties = new BasicProperties().builder().replyTo( consumeQueue).build();
        channel.basicPublish( publishExchange, publishQueue, properties, bytes);
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
    Channel publishChannel = publishChannelRef.get();
    if ( publishChannel != null && publishChannel.isOpen()) 
      return new SuccessAsyncFuture<ITransport>( this);
    
    try
    {
      // connection
      connectionFactory.setConnectionTimeout( timeout);
      connection = connectionFactory.newConnection(); // use connection pool, or define connection separately

      // consume
      Channel consumeChannel = connection.createChannel();
      DeclareOk declare = consumeChannel.queueDeclare();
      consumeQueue = declare.getQueue();
      consumeChannel.basicConsume( consumeQueue, false, "", new TransportConsumer( publishChannel));

      // publish
      publishChannel = connection.createChannel();
      publishChannelRef.set( publishChannel);
      
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
      publishChannelRef.set( null);
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
  
  @Override
  public void addRoute( String route, ITransport transport)
  {
    router.addRoute( route, transport);
  }

  @Override
  public void removeRoute( String route, ITransport transport)
  {
    router.removeRoute( route, transport);
  }

  @Override
  public Iterator<ITransport> resolve( String route)
  {
    return router.resolve( route);
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
  private String publishExchange;
  private String publishQueue;
  private String consumeQueue;
  private AtomicReference<Channel> publishChannelRef;
  private IRouter router;
}
