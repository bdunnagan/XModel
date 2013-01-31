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
import org.xmodel.GlobalSettings;
import org.xmodel.concurrent.SimpleThreadFactory;
import org.xmodel.xpath.expression.IContext;

/**
 * This class provides an interface for the server-side of the protocol.
 */
public class XioServer
{
  /**
   * Create a server that uses an NioServerSocketChannelFactory configured with tcp-no-delay and keep-alive.
   * @param bindContext The context for the remote bind protocol.
   * @param executeContext The context for the remote execution protocol.
   */
  public XioServer( IContext bindContext, IContext executeContext)
  {
    this( bindContext, executeContext, GlobalSettings.getInstance().getScheduler(), 
        Executors.newCachedThreadPool( new SimpleThreadFactory( "Server Boss")), 
        Executors.newCachedThreadPool( new SimpleThreadFactory( "Server Work")));
  }
  
  /**
   * Create a server that uses an NioServerSocketChannelFactory configured with tcp-no-delay and keep-alive.
   * @param bindContext The context for the remote bind protocol.
   * @param executeContext The context for the remote execution protocol.
   * @param scheduler The scheduler used for protocol timers.
   * @param bossExecutor The NioClientSocketChannelFactory boss executor.
   * @param workerExecutor The NioClientSocketChannelFactory worker executor.
   */
  public XioServer( IContext bindContext, IContext executeContext, ScheduledExecutorService scheduler, Executor bossExecutor, Executor workerExecutor)
  {
    handler = new XioChannelHandler( bindContext, executeContext, scheduler);
    
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
        
        XioChannelHandler handler = XioServer.this.handler;
        pipeline.addLast( "xio", sharedHandler? handler: new XioChannelHandler( handler));
        
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
      bootstrap.getFactory().releaseExternalResources();
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
      
      XioServer server = (XioServer)serverChannel.getAttachment();
      XioChannelHandler handler = server.handler;
      
      peer = new XioPeer( sharedHandler? handler: new XioChannelHandler( handler));
      channel.setAttachment( peer);
      return peer;
    }
  }
  
  private final static boolean sharedHandler = false;
  
  private ServerBootstrap bootstrap;
  private Channel serverChannel;
  private XioChannelHandler handler;
}
