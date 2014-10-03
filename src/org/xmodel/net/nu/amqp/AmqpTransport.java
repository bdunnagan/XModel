package org.xmodel.net.nu.amqp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;
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
import org.xmodel.util.PrefixThreadFactory;
import org.xmodel.xpath.expression.IContext;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

public class AmqpTransport extends AbstractTransport implements IRouter
{
  public static final String notConnectedError = "Not connected";
  
  public AmqpTransport( Protocol protocol, IContext transportContext)
  {
    this( protocol, transportContext, Executors.newScheduledThreadPool( 1, new PrefixThreadFactory( "scheduler")));
  }

  public AmqpTransport( Protocol protocol, IContext transportContext, ScheduledExecutorService scheduler)
  {
    super( protocol, transportContext, scheduler);
  
    publishChannelRef = new AtomicReference<Channel>();
    consumerChannels = Collections.synchronizedMap( new HashMap<String, Channel>());
    
    publishExchange = "";
    publishQueue = "";
    
    connectionFactory = new ConnectionFactory();
    connectionFactory.setHost( "localhost");
    
    router = new SimpleRouter();
    setRouter( this);
  }
  
  public void setRemoteAddress( InetSocketAddress address)
  {
    connectionFactory.setHost( address.getHostString());
    connectionFactory.setPort( address.getPort());
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
    Channel consumeChannel = consumerChannels.get( name);
    if ( consumeChannel != null) 
    {
      String message = String.format( "Transport already registered to %s", name);
      return new FailureAsyncFuture<ITransport>( this, message);
    }
    
    try
    {
      consumeChannel = connection.createChannel();
      consumerChannels.put( name, consumeChannel);
      
      consumeChannel.queueDeclare( name, false, true, true, Collections.<String, Object>emptyMap());
      consumeChannel.basicConsume( name, false, "", new TransportConsumer( consumeChannel));
    }
    catch( IOException e)
    {
      return new FailureAsyncFuture<ITransport>( this, e);
    }
    
    return super.register( name, messageContext, timeout);
  }

  @Override
  public AsyncFuture<ITransport> deregister( String name, IContext messageContext, int timeout)
  {
    Channel consumeChannel = consumerChannels.remove( name);
    if ( consumeChannel == null) return new SuccessAsyncFuture<ITransport>( this); 
    
    AsyncFuture<ITransport> future = super.deregister( name, messageContext, timeout);
    
    try
    {
      consumeChannel.queueDelete( name);
      consumeChannel.close();
    }
    catch( IOException e)
    {
      future.notifyFailure( e);
    }
    
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
        BasicProperties properties = new BasicProperties().builder().replyTo( replyQueue).build();
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
    if ( consumeQueue == null)
      return new FailureAsyncFuture<ITransport>( this, "Consumer exchange/queue not defined for transport.");
    
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
      //consumeChannel.queueDeclare( consumeQueue, false, true, true, null);
      consumeChannel.basicConsume( consumeQueue, false, "", new TransportConsumer( consumeChannel));
      consumerChannels.put( consumeQueue, consumeChannel);
      replyQueue = consumeQueue;

      // publish
      publishChannel = connection.createChannel();
      publishChannelRef.set( publishChannel);
      
      // send connect event
      try
      {
        getEventPipe().notifyConnect( getTransportContext());
      }
      catch( IOException e)
      {
        SLog.exception( this, e);
      }
      
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
      consumerChannels.clear();
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
    AmqpTransport newTransport = new AmqpTransport( getProtocol(), getTransportContext(), getScheduler());
    newTransport.setPublishQueue( route);
    newTransport.setConsumeQueue( queue);
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
  private Map<String, Channel> consumerChannels;
  private String replyQueue;
  private IRouter router;
}
