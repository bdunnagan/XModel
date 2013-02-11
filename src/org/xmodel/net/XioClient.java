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
import org.xmodel.concurrent.ModelThreadFactory;
import org.xmodel.xpath.expression.IContext;

/**
 * This class provides an interface for the client-side of the protocol.
 * (thread-safe)
 */
public class XioClient extends XioPeer
{
  /**
   * Create a client that uses an NioClientSocketChannelFactory configured with tcp-no-delay and keep-alive.
   * The executors for both protocols use GlobalSettings.getInstance().getModel().getExecutor() of this thread.
   * @param executor The executor that dispatches from the worker thread.
   */
  public XioClient( Executor executor)
  {
    this( null, null, null, null, executor);
  }
  
  /**
   * Create a client that uses an NioClientSocketChannelFactory configured with tcp-no-delay and keep-alive.
   * The executors for both protocols use GlobalSettings.getInstance().getModel().getExecutor() of this thread.
   * @param context The context.
   */
  public XioClient( IContext context)
  {
    this( context, null, null, null, context.getExecutor());
  }
  
  /**
   * Create a client that uses an NioClientSocketChannelFactory configured with tcp-no-delay and keep-alive. Null may be passed
   * for optional arguments. The following table shows defaults used for each optional argument:
   * <ul>
   *   <li>context - Required for remote execution from the server</li>
   *   <li>scheduler - GlobalSettings.getInstance().getScheduler()</li>
   *   <li>bossExecutor - Static ExecutorService.newCachedThreadPool</li>
   *   <li>workerExecutor - Static ExecutorService.newCachedThreadPool</li>
   * </ul>
   * @param context Optional context.
   * @param scheduler Optional scheduler used for protocol timers.
   * @param bossExecutor Optional NioClientSocketChannelFactory boss executor.
   * @param workerExecutor Optional oClientSocketChannelFactory worker executor.
   */
  public XioClient( final IContext context, final ScheduledExecutorService scheduler, Executor bossExecutor, Executor workerExecutor, final Executor contextExecutor)
  {
    if ( bossExecutor == null) bossExecutor = getDefaultBossExecutor();
    if ( workerExecutor == null) workerExecutor = getDefaultWorkerExecutor();
    
    this.scheduler = (scheduler != null)? scheduler: GlobalSettings.getInstance().getScheduler();
    
    bootstrap = new ClientBootstrap( new NioClientSocketChannelFactory( bossExecutor, workerExecutor));
    bootstrap.setOption( "tcpNoDelay", true);
    bootstrap.setOption( "keepAlive", true);
    
    bootstrap.setPipelineFactory( new ChannelPipelineFactory() {
      public ChannelPipeline getPipeline() throws Exception
      {
        ChannelPipeline pipeline = Channels.pipeline();
        pipeline.addLast( "xio", new XioChannelHandler( context, contextExecutor, scheduler));
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
   * Set the channel.
   * @param channel The channel.
   */
  private synchronized void setChannel( Channel channel)
  {
    this.channel = channel;
  }
  
  private static synchronized Executor getDefaultBossExecutor()
  {
    if ( defaultBossExecutor == null)
      defaultBossExecutor = Executors.newCachedThreadPool( new ModelThreadFactory( "xio-client-boss"));
    return defaultBossExecutor;
  }
  
  private static synchronized Executor getDefaultWorkerExecutor()
  {
    if ( defaultWorkerExecutor == null)
      defaultWorkerExecutor = Executors.newCachedThreadPool( new ModelThreadFactory( "xio-client-worker"));
    return defaultWorkerExecutor;
  }
  
  private static Executor defaultBossExecutor = null;
  private static Executor defaultWorkerExecutor = null;
  
  private ClientBootstrap bootstrap;
  private ScheduledExecutorService scheduler;
}
