package org.xmodel.net;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.xmodel.GlobalSettings;
import org.xmodel.concurrent.SimpleThreadFactory;
import org.xmodel.xpath.expression.IContext;

/**
 * This class provides an interface for the client-side of the protocol.
 * (thread-safe)
 */
public class XioClient extends XioPeer
{
  /**
   * Create a client that uses an NioClientSocketChannelFactory configured with tcp-no-delay and keep-alive.
   * Clients created with this constructor cannot handle incoming requests.
   */
  public XioClient()
  {
    this( null, true, GlobalSettings.getInstance().getScheduler(), getDefaultExecutor(), getDefaultExecutor());
  }
  
  /**
   * Create a client that uses an NioClientSocketChannelFactory configured with tcp-no-delay and keep-alive.
   * @param context The context.
   */
  public XioClient( IContext context)
  {
    this( context, false, GlobalSettings.getInstance().getScheduler(), getDefaultExecutor(), context.getModel().getDispatcher());
  }
  
  private static synchronized Executor getDefaultExecutor()
  {
    if ( defaultExecutor == null)
      defaultExecutor = Executors.newCachedThreadPool( new SimpleThreadFactory( "Client IO"));
    return defaultExecutor;
  }
  
  /**
   * Create a client that uses an NioClientSocketChannelFactory configured with tcp-no-delay and keep-alive.
   * @param context The context.
   * @param dispatch True if model events should be dispatched to the context model dispatcher.
   * @param scheduler The scheduler used for protocol timers.
   * @param bossExecutor The NioClientSocketChannelFactory boss executor.
   * @param workerExecutor The NioClientSocketChannelFactory worker executor.
   */
  public XioClient( final IContext context, final boolean dispatch, final ScheduledExecutorService scheduler, Executor bossExecutor, Executor workerExecutor)
  {
    this.scheduler = scheduler;
    
    bootstrap = new ClientBootstrap( new NioClientSocketChannelFactory( bossExecutor, workerExecutor));
    bootstrap.setOption( "tcpNoDelay", true);
    bootstrap.setOption( "keepAlive", true);
    
    bootstrap.setPipelineFactory( new ChannelPipelineFactory() {
      public ChannelPipeline getPipeline() throws Exception
      {
        ChannelPipeline pipeline = Channels.pipeline();
        pipeline.addLast( "xio", new XioChannelHandler( context, dispatch, scheduler));
        return pipeline;
      }
    });
  }
  
  /**
   * Connect this client to the specified server. A client may only be connected to one server.
   * @param address The address of the server.
   * @param port The port of the server.
   * @return Returns true if the connection was established.
   */
  public ChannelFuture connect( String address, int port)
  {
    return connect( new InetSocketAddress( address, port));
  }
  
  /**
   * Attempt to connect this client to the specified server the specified number of times.
   * @param address The address of the server.
   * @param port The port of the server.
   * @param retries The maximum number of retries.
   * @param delay The delay between retries in milliseconds.
   * @return Returns a future that is retry-aware.
   */
  public ConnectFuture connect( String address, int port, int retries, int delay)
  {
    return connect( new InetSocketAddress( address, port), retries, new int[] { delay});
  }
  
  /**
   * Attempt to connect this client to the specified server the specified number of times.
   * @param address The address of the server.
   * @param port The port of the server.
   * @param delays An array of delays between retries in milliseconds.
   * @return Returns a future that is retry-aware.
   */
  public ConnectFuture connect( String address, int port, int[] delays)
  {
    return connect( new InetSocketAddress( address, port), delays.length, delays);
  }
  
  /**
   * Attempt to connect this client to the specified server the specified number of times.
   * @param address The address of the server.
   * @param port The port of the server.
   * @param retries The maximum number of retries.
   * @param delays An array of delays between retries in milliseconds.
   * @return Returns a future that is retry-aware.
   */
  public ConnectFuture connect( String address, int port, int retries, int[] delays)
  {
    return connect( new InetSocketAddress( address, port), retries, delays);
  }
  
  /**
   * Connect this client to the specified server. A client may only be connected to one server.
   * @param address The address of the server.
   * @return Returns true if the connection was established.
   */
  public ChannelFuture connect( InetSocketAddress address)
  {
    ChannelFuture future = bootstrap.connect( address);
    channel = future.getChannel();
    return future;
  }
  
  /**
   * Attempt to connect this client to the specified server the specified number of times.
   * @param address The address of the server.
   * @param port The port of the server.
   * @param retries The maximum number of retries.
   * @param delay The delay between retries in milliseconds.
   * @return Returns a future that is retry-aware.
   */
  public ConnectFuture connect( InetSocketAddress address, int retries, int delay)
  {
    return connect( address, retries, new int[] { delay});
  }
  
  /**
   * Attempt to connect this client to the specified server the specified number of times.
   * @param address The address of the server.
   * @param port The port of the server.
   * @param delays An array of delays between retries in milliseconds.
   * @return Returns a future that is retry-aware.
   */
  public ConnectFuture connect( InetSocketAddress address, int[] delays)
  {
    return connect( address, delays.length, delays);
  }
  
  /**
   * Attempt to connect this client to the specified server the specified number of times.
   * @param address The address of the server.
   * @param retries The maximum number of retries.
   * @param delays An array of delays between retries in milliseconds.
   * @return Returns a future that is retry-aware.
   */
  public ConnectFuture connect( InetSocketAddress address, int retries, int[] delays)
  {
    ConnectFuture future = new ConnectFuture( bootstrap, address, scheduler, retries, delays);
    future.addListener( new ChannelFutureListener() {
      public void operationComplete( ChannelFuture future) throws Exception
      {
        if ( future.isSuccess()) setChannel( future.getChannel());
      }
    });
    return future;
  }
  
  /**
   * @return Returns true if the connection to the server is established.
   */
  public synchronized boolean isConnected()
  {
    return (channel != null)? channel.isConnected(): false;
  }
  
  /**
   * Close the connection.
   */
  public void close()
  {
    if ( isConnected())
    {
      channel.close().awaitUninterruptibly();
    }
  }
  
  /**
   * @return Returns the remote address to which this client is connected.
   */
  public synchronized InetSocketAddress getRemoteAddress()
  {
    return (channel != null)? (InetSocketAddress)channel.getRemoteAddress(): null;
  }
  
  /**
   * Set the channel.
   * @param channel The channel.
   */
  private synchronized void setChannel( Channel channel)
  {
    this.channel = channel;
  }
  
  private static Executor defaultExecutor = null;
  
  private ClientBootstrap bootstrap;
  private ScheduledExecutorService scheduler;
}
