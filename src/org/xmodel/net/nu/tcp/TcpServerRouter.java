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
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.ReadWriteLock;
import org.xmodel.future.AsyncFuture;
import org.xmodel.net.nu.IConnectListener;
import org.xmodel.net.nu.IDisconnectListener;
import org.xmodel.net.nu.IErrorListener;
import org.xmodel.net.nu.IReceiveListener;
import org.xmodel.net.nu.IRouter;
import org.xmodel.net.nu.ITransport;
import org.xmodel.net.nu.ITransportImpl;
import org.xmodel.net.nu.ReliableTransport;
import org.xmodel.net.nu.protocol.Protocol;
import org.xmodel.util.PrefixThreadFactory;
import org.xmodel.xpath.expression.IContext;

public class TcpServerRouter implements IRouter
{
  public TcpServerRouter( Protocol protocol, IContext transportContext, boolean reliable)
  {
    this( protocol, transportContext, null);
    this.reliable = reliable;
  }
  
  public TcpServerRouter( Protocol protocol, IContext transportContext, ScheduledExecutorService scheduler)
  {
    if ( scheduler == null) scheduler = Executors.newScheduledThreadPool( 1, new PrefixThreadFactory( "scheduler"));
    
    this.protocol = protocol;
    this.transportContext = transportContext;
    this.scheduler = scheduler;
    this.routes = new HashMap<String, Set<ITransport>>();
    
    this.connectListeners = new ArrayList<IConnectListener>( 3);
    this.disconnectListeners = new ArrayList<IDisconnectListener>( 3);
    this.receiveListeners = new ArrayList<IReceiveListener>( 3);
    this.errorListeners = new ArrayList<IErrorListener>( 3);
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
         ITransportImpl transport = new TcpChildTransport( protocol, transportContext, scheduler, channel);
         if ( reliable) transport = new ReliableTransport( transport);
         
         for( IConnectListener listener: connectListeners) transport.addListener( listener);
         for( IDisconnectListener listener: disconnectListeners) transport.addListener( listener);
         for( IReceiveListener listener: receiveListeners) transport.addListener( listener);
         for( IErrorListener listener: errorListeners) transport.addListener( listener);
           
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
    try
    {
      routesLock.writeLock().lock();
      Set<ITransport> transports = routes.get( route);
      if ( transports == null)
      {
        transports = new HashSet<ITransport>();
        routes.put( route, transports);
      }
      transports.add( transport);
    }
    finally
    {
      routesLock.writeLock().unlock();
    }
  }

  @Override
  public void removeRoute( String route, ITransport transport)
  {
    try
    {
      routesLock.writeLock().lock();
      Set<ITransport> transports = routes.get( route);
      if ( transports != null) transports.remove( transport);
      if ( transports.size() == 0) routes.remove( route);
    }
    finally
    {
      routesLock.writeLock().unlock();
    }
  }

  @Override
  public Iterator<ITransport> resolve( String route)
  {
    try
    {
      routesLock.readLock().lock();
      Set<ITransport> transports = routes.get( route);
      if ( transports != null)
      {
        ArrayList<ITransport> copy = new ArrayList<ITransport>( transports);
        return copy.iterator();
      }
      else
      {
        return Collections.<ITransport>emptyList().iterator();
      }
    }
    finally
    {
      routesLock.readLock().unlock();
    }
  }
  
  public void addListener( IConnectListener listener)
  {
    if ( !connectListeners.contains( listener))
      connectListeners.add( listener);
  }

  public void removeListener( IConnectListener listener)
  {
    connectListeners.remove( listener);
  }

  public void addListener( IDisconnectListener listener)
  {
    if ( !disconnectListeners.contains( listener))
      disconnectListeners.add( listener);
  }

  public void removeListener( IDisconnectListener listener)
  {
    disconnectListeners.remove( listener);
  }

  public void addListener( IReceiveListener listener)
  {
    if ( !receiveListeners.contains( listener))
      receiveListeners.add( listener);
  }

  public void removeListener( IReceiveListener listener)
  {
    receiveListeners.remove( listener);
  }

  public void addListener( IErrorListener listener)
  {
    if ( !errorListeners.contains( listener))
      errorListeners.add( listener);
  }

  public void removeListener( IErrorListener listener)
  {
    errorListeners.remove( listener);
  }

  private Protocol protocol;
  private IContext transportContext;
  private ScheduledExecutorService scheduler;
  private ServerSocketChannel serverChannel;
  private Map<String, Set<ITransport>> routes;
  private ReadWriteLock routesLock;
  private boolean reliable;
  private List<IConnectListener> connectListeners;
  private List<IDisconnectListener> disconnectListeners;
  private List<IReceiveListener> receiveListeners;
  private List<IErrorListener> errorListeners;
}