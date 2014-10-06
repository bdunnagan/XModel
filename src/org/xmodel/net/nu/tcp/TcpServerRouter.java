package org.xmodel.net.nu.tcp;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.xmodel.IModelObject;
import org.xmodel.future.AsyncFuture;
import org.xmodel.net.nu.IEventHandler;
import org.xmodel.net.nu.IRouter;
import org.xmodel.net.nu.ITransport;
import org.xmodel.net.nu.ITransport.Error;
import org.xmodel.net.nu.ITransportImpl;
import org.xmodel.net.nu.ReliableTransport;
import org.xmodel.net.nu.SimpleRouter;
import org.xmodel.net.nu.protocol.Protocol;
import org.xmodel.util.PrefixThreadFactory;
import org.xmodel.xpath.expression.IContext;

public class TcpServerRouter implements IRouter
{
  public TcpServerRouter( Protocol protocol, IContext transportContext, boolean reliable)
  {
    this( protocol, transportContext, null, reliable);
  }
  
  public TcpServerRouter( Protocol protocol, IContext transportContext, ScheduledExecutorService scheduler, boolean reliable)
  {
    if ( scheduler == null) scheduler = Executors.newScheduledThreadPool( 1, new PrefixThreadFactory( "scheduler"));
    
    this.protocol = protocol;
    this.transportContext = transportContext;
    this.scheduler = scheduler;
    this.reliable = reliable;
    this.router = new SimpleRouter();
  }
  
  public void setEventHandler( ITcpServerEventHandler eventHandler)
  {
    this.eventHandler = eventHandler;
  }
  
  public void start( SocketAddress address) throws InterruptedException
  {
    EventLoopGroup bossGroup = new NioEventLoopGroup(); 
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    
    ServerBootstrap bootstrap = new ServerBootstrap();
    bootstrap.group( bossGroup, workerGroup);
    bootstrap.channel( NioServerSocketChannel.class);
    
    bootstrap.childHandler( new ChannelInitializer<SocketChannel>() {
       @Override
       public void initChannel( SocketChannel channel) throws Exception 
       {
         ITransportImpl transport = new TcpChildTransport( TcpServerRouter.this, protocol, transportContext, scheduler, channel);
         if ( reliable) transport = new ReliableTransport( transport);
         
         if ( eventHandler != null) transport.getEventPipe().addLast( new EventHandler( transport, eventHandler));
         
         transport.connect( 0); // notify listeners of new connection
         channel.pipeline().addLast( new XioInboundHandler( transport));
       }
     });

    // options
    bootstrap.option( ChannelOption.SO_BACKLOG, 128);          
    bootstrap.childOption( ChannelOption.AUTO_READ, false);

    // bind and start accepting connections
    ChannelFuture future = bootstrap.bind( address);
    
    // wait for operation to complete
    future.sync();
    
    // store server channel
    serverChannel = (ServerSocketChannel)future.channel();
  }
  
  public AsyncFuture<TcpServerRouter> close()
  {
    serverChannel.close();
    
    AsyncFuture<TcpServerRouter> future = new AsyncFuture<TcpServerRouter>( this);
    serverChannel.closeFuture().addListener( new AsyncFutureAdapter<TcpServerRouter>( future));
    return future;
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

  static class EventHandler implements IEventHandler
  {
    public EventHandler( ITransport transport, ITcpServerEventHandler eventHandler)
    {
      this.transport = transport;
      this.eventHandler = eventHandler;
    }
    
    @Override
    public boolean notifyConnect( IContext transportContext) throws IOException
    {
      eventHandler.notifyConnect( transport, transportContext);
      return false;
    }

    @Override
    public boolean notifyDisconnect( IContext transportContext) throws IOException
    {
      eventHandler.notifyDisconnect( transport, transportContext);
      return false;
    }

    @Override
    public boolean notifyReceive( ByteBuffer buffer) throws IOException
    {
      return false;
    }

    @Override
    public boolean notifyReceive( IModelObject envelope)
    {
      return false;
    }

    @Override
    public boolean notifyReceive( IModelObject message, IContext messageContext, IModelObject request)
    {
      eventHandler.notifyReceive( transport, message, messageContext, request);
      return false;
    }

    @Override
    public boolean notifyError( IContext context, Error error, IModelObject request)
    {
      eventHandler.notifyError( transport, context, error, request);
      return false;
    }

    @Override
    public boolean notifyException( IOException e)
    {
      eventHandler.notifyException( transport, e);
      return false;
    }

    private ITransport transport;
    private ITcpServerEventHandler eventHandler;
  }
  
  private Protocol protocol;
  private IContext transportContext;
  private ScheduledExecutorService scheduler;
  private ServerSocketChannel serverChannel;
  private SimpleRouter router;
  private boolean reliable;
  private ITcpServerEventHandler eventHandler;
}
