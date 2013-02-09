package org.xmodel.net;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.util.ThreadNameDeterminer;
import org.jboss.netty.util.ThreadRenamingRunnable;
import org.xmodel.GlobalSettings;
import org.xmodel.concurrent.SimpleThreadFactory;
import org.xmodel.xpath.expression.IContext;

/**
 * This class provides an interface for the server-side of the protocol.
 */
public class XioServer
{
  /**
   * Create a client that uses an NioClientSocketChannelFactory configured with tcp-no-delay and keep-alive.
   * The executors for both protocols use GlobalSettings.getInstance().getModel().getExecutor() of this thread.
   * @param context The context.
   */
  public XioServer( IContext context)
  {
    this( context, null, null, null, GlobalSettings.getInstance().getModel().getExecutor(), GlobalSettings.getInstance().getModel().getExecutor());
  }
  
  /**
   * Create a server that uses an NioServerSocketChannelFactory configured with tcp-no-delay and keep-alive. Null may be passed
   * for optional arguments. The following table shows defaults used for each optional argument:
   * <ul>
   *   <li>context - Required for remote execution from the server</li>
   *   <li>scheduler - GlobalSettings.getInstance().getScheduler()</li>
   *   <li>bossExecutor - Static ExecutorService.newCachedThreadPool</li>
   *   <li>workerExecutor - Static ExecutorService.newCachedThreadPool</li>
   *   <li>bindProtocolExecutor - Null executor means immediate processing in worker thread</li>
   *   <li>executionProtocolExecutor - Null executor means immediate processing in worker thread</li>
   * </ul>
   * @param context Optional context.
   * @param scheduler Optional scheduler used for protocol timers.
   * @param bossExecutor Optional NioClientSocketChannelFactory boss executor.
   * @param workerExecutor Optional oClientSocketChannelFactory worker executor.
   * @param bindProtocolExecutor Optional executor for dispatching bind requests out of the I/O worker thread.
   * @param executeProtocolExecutor Optional executor for dispatching remote execution requests out of the I/O worker thread.
   */
  public XioServer( 
      final IContext context, 
      final ScheduledExecutorService scheduler, 
      Executor bossExecutor, 
      Executor workerExecutor, 
      final Executor bindProtocolExecutor,
      final Executor executeProtocolExecutor)
  {
    ThreadRenamingRunnable.setThreadNameDeterminer( ThreadNameDeterminer.CURRENT);    
    
    if ( bossExecutor == null) bossExecutor = getDefaultBossExecutor();
    if ( workerExecutor == null) workerExecutor = getDefaultWorkerExecutor();
    
    bootstrap = new ServerBootstrap( new NioServerSocketChannelFactory( bossExecutor, workerExecutor));
    bootstrap.setOption( "tcpNoDelay", true);
    bootstrap.setOption( "keepAlive", true);
    
    bootstrap.setPipelineFactory( new ChannelPipelineFactory() {
      public ChannelPipeline getPipeline() throws Exception
      {
        ChannelPipeline pipeline = Channels.pipeline();
        
//        SSLEngine engine = SecureChatSslContextFactory.getServerContext().createSSLEngine();
//        engine.setUseClientMode(false);
//
//        pipeline.addLast( "ssl", new SslHandler(engine));
        
        pipeline.addLast( "xio", new XioChannelHandler( context, scheduler, bindProtocolExecutor, executeProtocolExecutor));
        return pipeline;
      }
    });
  }
  
  /**
   * Start the server on the specified address and port. 
   * @param address The server address.
   * @param port The server port.
   * @return Returns the channel instance of the server socket.
   */
  public Channel start( String address, int port)
  {
    serverChannel = bootstrap.bind( new InetSocketAddress( address, port));
    serverChannel.setAttachment( this);
    return serverChannel;
  }
  
  /**
   * Stop the server from listening for new connections.
   */
  public void stop()
  {
    if ( serverChannel != null) 
    {
      serverChannel.close().awaitUninterruptibly();
    }
  }
  
  /**
   * Get and/or create an XioPeer instance to represent the specified server connected channel.
   * @param channel A connected channel.
   * @return Returns null or the new XioPeer instance.
   */
  public static XioPeer getPeer( Channel channel)
  {
    synchronized( channel)
    {
      XioPeer peer = (XioPeer)channel.getAttachment();
      if ( peer != null) return peer;
    
      Channel serverChannel = channel.getParent();
      if ( serverChannel == null) return null;
      
      peer = new XioPeer();
      channel.setAttachment( peer);
      return peer;
    }
  }
  
  private static synchronized Executor getDefaultBossExecutor()
  {
    if ( defaultBossExecutor == null)
      defaultBossExecutor = Executors.newCachedThreadPool( new SimpleThreadFactory( "xio-server-boss"));
    return defaultBossExecutor;
  }
  
  private static synchronized Executor getDefaultWorkerExecutor()
  {
    if ( defaultWorkerExecutor == null)
      defaultWorkerExecutor = Executors.newCachedThreadPool( new SimpleThreadFactory( "xio-server-work"));
    return defaultWorkerExecutor;
  }
  
  private static Executor defaultBossExecutor = null;
  private static Executor defaultWorkerExecutor = null;
  
  private ServerBootstrap bootstrap;
  private Channel serverChannel;
}
