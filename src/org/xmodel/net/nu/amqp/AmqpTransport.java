package org.xmodel.net.nu.amqp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
import org.xmodel.net.nu.algo.ReconnectAlgo;
import org.xmodel.net.nu.protocol.IEnvelopeProtocol;
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
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

public class AmqpTransport extends AbstractTransport implements IRouter
{
  public static final String notConnectedError = "Not connected";
  
  public AmqpTransport( Protocol protocol, IContext transportContext)
  {
    super( protocol, transportContext);
    
    publishChannelRef = new AtomicReference<Channel>();
    consumerChannels = Collections.synchronizedMap( new HashMap<String, Channel>());
    
    publishExchange = "";
    publishQueue = "";
    
    connectionFactory = new ConnectionFactory();
    connectionFactory.setHost( "localhost");
    connectionFactory.setRequestedHeartbeat( 30);
    
    router = new SimpleRouter();
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
  
  public void setConsumeQueue( String queue, boolean purge)
  {
    this.consumeQueue = queue;
    this.purgeConsumeQueue = purge;
  }

  public void setAmqpHeartbeatTimeout( int timeout)
  {
    connectionFactory.setRequestedHeartbeat( timeout / 1000);
  }
  
  @Override
  public AsyncFuture<ITransport> sendImpl( IModelObject envelope, IModelObject request)
  {
    String replyQueue = getProtocol().envelope().getReplyTo( request);
    return publish( publishExchange, (replyQueue != null)? replyQueue: publishQueue, envelope, request);
  }
  
  protected AsyncFuture<ITransport> publish( String publishExchange, String publishQueue, IModelObject envelope, IModelObject request)
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
    
    IEnvelopeProtocol envelopeProtocol = getProtocol().envelope();
    if ( envelopeProtocol.isRequest( envelope))
      envelopeProtocol.setReplyTo( envelope, consumeQueue);
    
    // encode
    byte[] bytes = encode( envelope, future);
    if ( bytes != null)
    {
      try
      {
        // publish
        String replyTo = (request != null)? envelopeProtocol.getReplyTo( request): null;
        if ( replyTo != null)
        {
          channel.basicPublish( "", replyTo, null, bytes);
        }
        else
        {
          channel.basicPublish( publishExchange, publishQueue, null, bytes);
        }
      }
      catch( IOException e)
      {
        future.notifyFailure( e);
      }
    }
    
    future.notifySuccess();
    return future;
  }

  @Override
  public void setConnectTimeout( int timeout)
  {
    connectionFactory.setConnectionTimeout( timeout);
  }

  @Override
  public AsyncFuture<ITransport> connect()
  {    
    Channel publishChannel = publishChannelRef.get();
    if ( publishChannel != null && publishChannel.isOpen()) 
      return new SuccessAsyncFuture<ITransport>( this);
    
    try
    {
      // connection
      connection = connectionFactory.newConnection(); // use connection pool, or define connection separately
      connection.addShutdownListener( connectionShutdownListener);

      // consume initially on temporary queue
      Channel consumeChannel = connection.createChannel();
      
      if ( consumeQueueIsTemp) consumeQueue = null;
      
      if ( consumeQueue != null)
      {
        try
        {
          consumeChannel.queueDeclare( consumeQueue, false, true, true, Collections.<String, Object>emptyMap());
          consumeQueueIsTemp = false;
        }
        catch( IOException e)
        {
          consumeChannel = connection.createChannel();
        }
      }
      else
      {
        DeclareOk declareOk = consumeChannel.queueDeclare();
        consumeQueue = declareOk.getQueue();
        consumeQueueIsTemp = true;
      }
      
      if ( purgeConsumeQueue) consumeChannel.queuePurge( consumeQueue);
      
      consumeChannel.basicConsume( consumeQueue, true, "", new TransportConsumer( consumeChannel));
      consumerChannels.put( consumeQueue, consumeChannel);

      // publish
      publishChannel = connection.createChannel();
      publishChannelRef.set( publishChannel);
      
      // send connect event
      try
      {
        getEventPipe().notifyConnect( this, getTransportContext());
      }
      catch( IOException e)
      {
        SLog.exception( this, e);
      }
      
      return new SuccessAsyncFuture<ITransport>( this);
    }
    catch( IOException e)
    {
      log.exception( e);
      getEventPipe().notifyError( this, getTransportContext(), Error.connectError, null);
      return new FailureAsyncFuture<ITransport>( this, e);
    }
  }

  @Override
  public AsyncFuture<ITransport> disconnect( boolean reconnect)
  {
    ReconnectAlgo algo = (ReconnectAlgo)getEventPipe().getHandler( ReconnectAlgo.class);
    if ( algo != null) algo.setReconnect( reconnect);
    
    try
    {
      publishChannelRef.set( null);
      consumerChannels.clear();
      connection.removeShutdownListener( connectionShutdownListener);
      connection.close();
    }
    catch( IOException e)
    {
      log.exception( e);
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
      getEventPipe().notifyError( this, getTransportContext(), ITransport.Error.encodeFailed, null);
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
  public Iterator<String> removeRoutes( ITransport transport)
  {
    return router.removeRoutes( transport);
  }
  
  @Override
  public boolean hasRoute( String route)
  {
    return router.hasRoute( route);
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
      getEventPipe().notifyReceive( AmqpTransport.this, ByteBuffer.wrap( body));
    }

    @Override
    public void handleShutdownSignal( String consumerTag, ShutdownSignalException signal)
    {
      Object reason = signal.getReason();
      if ( !(reason instanceof AMQP.Channel.Close) || ((AMQP.Channel.Close)reason).getReplyCode() != 200)
      {
        log.exceptionf( signal, "Unhandled exception ...");
      }
      
      try
      {
        getEventPipe().notifyDisconnect( AmqpTransport.this, getTransportContext());
      }
      catch( IOException e)
      {
        log.exception( e);
      }
    }
  };
  
  private final ShutdownListener connectionShutdownListener = new ShutdownListener() {
    @Override
    public void shutdownCompleted( ShutdownSignalException e)
    {
      getEventPipe().notifyError( AmqpTransport.this, getTransportContext(), Error.connectError, null);
    }
  };
  
  private ConnectionFactory connectionFactory;
  private Connection connection;
  private String publishExchange;
  private String publishQueue;
  private String consumeQueue;
  private boolean consumeQueueIsTemp;
  private boolean purgeConsumeQueue;
  private AtomicReference<Channel> publishChannelRef;
  private Map<String, Channel> consumerChannels;
  private IRouter router;
}
