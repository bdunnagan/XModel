package org.xmodel.net.nu.amqp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import org.xmodel.IModelObject;
import org.xmodel.future.AsyncFuture;
import org.xmodel.future.FailureAsyncFuture;
import org.xmodel.future.SuccessAsyncFuture;
import org.xmodel.log.SLog;
import org.xmodel.net.nu.AbstractTransport;
import org.xmodel.net.nu.EventPipe;
import org.xmodel.net.nu.IRouter;
import org.xmodel.net.nu.ITransport;
import org.xmodel.net.nu.ITransportImpl;
import org.xmodel.net.nu.algo.MuxAlgo;
import org.xmodel.net.nu.algo.RequestTrackingAlgo;
import org.xmodel.net.nu.protocol.IEnvelopeProtocol;
import org.xmodel.net.nu.protocol.IEnvelopeProtocol.Type;
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

public class AmqpTransport extends AbstractTransport implements IRouter
{
  public static final String notConnectedError = "Not connected";
  
  public AmqpTransport( Protocol protocol, IContext transportContext, ScheduledExecutorService scheduler)
  {
    super( protocol, transportContext);

    if ( scheduler == null) scheduler = Executors.newScheduledThreadPool( 1);
    this.scheduler = scheduler;
    
    publishChannelRef = new AtomicReference<Channel>();
    consumerChannels = Collections.synchronizedMap( new HashMap<String, Channel>());
    
    publishExchange = "";
    publishQueue = "";
    
    connectionFactory = new ConnectionFactory();
    connectionFactory.setHost( "localhost");
    connectionFactory.setRequestedHeartbeat( 30);
    
    routes = new HashMap<String, AmqpNamedTransport>();
    mux = new ConcurrentHashMap<Long, ITransportImpl>();
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

  public void setHeartbeatTimeout( int timeout)
  {
    connectionFactory.setRequestedHeartbeat( timeout);
  }
  
  public ConcurrentHashMap<Long, ITransportImpl> getMuxMap()
  {
    return mux;
  }
  
  @Override
  public boolean notifySend( ITransportImpl transport, IModelObject envelope, IContext messageContext, int timeout, int retries, int life)
  {
    Type type = getProtocol().envelope().getType( envelope);
    switch( type)
    {
      case register:   return createRegistrationQueue( envelope, messageContext);
      case deregister: return deleteRegistrationQueue( envelope, messageContext);
      default: break; 
    }
    
    return false;
  }

  public boolean createRegistrationQueue( IModelObject envelope, IContext messageContext)
  {
    String name = getProtocol().envelope().getRegistrationName( envelope);
    
    if ( tempQueue != null)
    {
      Channel consumeChannel = consumerChannels.remove( tempQueue);
      if ( consumeChannel != null)
      {
        try
        {
          consumeChannel.queueDelete( tempQueue);
          consumeChannel.close();
        }
        catch( IOException e)
        {
          log.warnf( "Failed to delete temporary queue, %s", tempQueue);
        }
        
        replyQueue = name;
        tempQueue = null;
      }
    }
    
    Channel consumeChannel = consumerChannels.get( name);
    if ( consumeChannel != null) 
    {
      log.errorf( "Transport already registered to %s", name);
      getEventPipe().notifyError( this, messageContext, Error.sendFailed, envelope);
      return false;
    }
    
    try
    {
      consumeChannel = connection.createChannel();
      consumerChannels.put( name, consumeChannel);
      
      consumeChannel.queueDeclare( name, false, true, true, Collections.<String, Object>emptyMap());
      consumeChannel.basicConsume( name, true, "", new TransportConsumer( consumeChannel));
    }
    catch( IOException e)
    {
      getEventPipe().notifyException( this, e);
      return false;
    }
    
    return false;
  }

  public boolean deleteRegistrationQueue( IModelObject envelope, IContext messageContext)
  {
    String name = getProtocol().envelope().getRegistrationName( envelope);
    
    Channel consumeChannel = consumerChannels.remove( name);
    if ( consumeChannel == null) return false; 
    
    try
    {
      consumeChannel.queueDelete( name);
      consumeChannel.close();
    }
    catch( IOException e)
    {
      getEventPipe().notifyException( this, e);
    }

    if ( replyQueue.equals( name))
    {
      try
      {
        // resume consuming on temporary queue
        consumeChannel = connection.createChannel();
        DeclareOk declareOk = consumeChannel.queueDeclare();
        tempQueue = replyQueue = consumeQueue = declareOk.getQueue();
        consumeChannel.basicConsume( consumeQueue, false, "", new TransportConsumer( consumeChannel));
        consumerChannels.put( consumeQueue, consumeChannel);
      }
      catch( IOException e)
      {
        getEventPipe().notifyException( this, e);
      }
    }
    
    return false;
  }
  
  @Override
  public AsyncFuture<ITransport> sendImpl( IModelObject envelope, IModelObject request)
  {
    return publish( publishExchange, publishQueue, replyQueue, envelope, request);
  }
  
  protected AsyncFuture<ITransport> publish( String publishExchange, String publishQueue, String replyQueue, IModelObject envelope, IModelObject request)
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
    envelopeProtocol.setReplyTo( envelope, replyQueue);
    
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
    if ( consumeQueue == null)
      return new FailureAsyncFuture<ITransport>( this, "Consumer exchange/queue not defined for transport.");
    
    Channel publishChannel = publishChannelRef.get();
    if ( publishChannel != null && publishChannel.isOpen()) 
      return new SuccessAsyncFuture<ITransport>( this);
    
    try
    {
      // connection
      connection = connectionFactory.newConnection(); // use connection pool, or define connection separately

      // consume initially on temporary queue
      Channel consumeChannel = connection.createChannel();
      
      if ( consumeQueue != null)
      {
        try
        {
          consumeChannel.queueDeclare( consumeQueue, false, true, true, Collections.<String, Object>emptyMap());
        }
        catch( IOException e)
        {
          consumeChannel = connection.createChannel();
        }
      }
      else
      {
        DeclareOk declareOk = consumeChannel.queueDeclare();
        tempQueue = replyQueue = consumeQueue = declareOk.getQueue();
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
    // TODO: this is slightly incorrect in that transport arg may be AmqpTransport
    synchronized( routes)
    {
      AmqpNamedTransport childTransport = routes.get( route);
      if ( childTransport == null)
      {
        childTransport = new AmqpNamedTransport( route, consumeQueue, this);
        EventPipe eventPipe = childTransport.getEventPipe();
        eventPipe.addFirst( new MuxAlgo( mux));
        eventPipe.addFirst( new RequestTrackingAlgo( scheduler));
        childTransport.connect();
        routes.put( route, childTransport);
      }
      childTransport.incrementReferenceCount();
    }
  }

  @Override
  public void removeRoute( String route, ITransport transport)
  {
    // TODO: this is slightly incorrect in that transport arg may be AmqpTransport
    synchronized( routes)
    {
      AmqpNamedTransport childTransport = routes.get( route);
      if ( childTransport != null && childTransport.decrementReferenceCount() == 0)
      {
        //
        // AmqpNamedTransport will call this method when heartbeat is lost.  In this case,
        // this method must verify that a new AmqpNamedTransport has not already reconnected.
        //
        if ( transport instanceof AmqpTransport || transport == childTransport)
          routes.remove( route);
      }
    }
  }

  @Override
  public void removeRoutes( ITransport transport)
  {
    if ( transport instanceof AmqpNamedTransport)
    {
      String route = ((AmqpNamedTransport)transport).getPublishQueue();
      removeRoute( route, transport);
    }
  }
  
  @Override
  public Iterator<ITransport> resolve( String route)
  {
    synchronized( routes)
    {
      AmqpNamedTransport childTransport = routes.get( route);
      return (childTransport != null)? Collections.<ITransport>singletonList( childTransport).iterator(): Collections.<ITransport>emptyList().iterator();
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
  
  private ConnectionFactory connectionFactory;
  private Connection connection;
  private String publishExchange;
  private String publishQueue;
  private String consumeQueue;
  private boolean purgeConsumeQueue;
  private String tempQueue;
  private String replyQueue;
  private AtomicReference<Channel> publishChannelRef;
  private Map<String, Channel> consumerChannels;
  private Map<String, AmqpNamedTransport> routes;
  private ConcurrentHashMap<Long, ITransportImpl> mux;
  private ScheduledExecutorService scheduler;
}
