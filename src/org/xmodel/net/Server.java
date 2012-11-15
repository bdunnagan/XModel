package org.xmodel.net;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.xmodel.GlobalSettings;
import org.xmodel.xpath.expression.IContext;

/**
 * This class provides an interface for the server-side of the protocol.
 */
public class Server extends Peer
{
  /**
   * Create a server that uses an NioServerSocketChannelFactory configured with tcp-no-delay and keep-alive.
   * @param bindContext The context for the remote bind protocol.
   * @param executeContext The context for the remote execution protocol.
   */
  public Server( IContext bindContext, IContext executeContext)
  {
    this( bindContext, executeContext, GlobalSettings.getInstance().getScheduler(), Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
  }
  
  /**
   * Create a server that uses an NioServerSocketChannelFactory configured with tcp-no-delay and keep-alive.
   * @param bindContext The context for the remote bind protocol.
   * @param executeContext The context for the remote execution protocol.
   * @param scheduler The scheduler used for protocol timers.
   * @param bossExecutor The NioClientSocketChannelFactory boss executor.
   * @param workerExecutor The NioClientSocketChannelFactory worker executor.
   */
  public Server( IContext bindContext, IContext executeContext, ScheduledExecutorService scheduler, Executor bossExecutor, Executor workerExecutor)
  {
    super( bindContext, executeContext, scheduler);
    
    bootstrap = new ServerBootstrap( new NioServerSocketChannelFactory( bossExecutor, workerExecutor));
    bootstrap.setPipelineFactory( this);
    bootstrap.setOption( "tcpNoDelay", true);
    bootstrap.setOption( "keepAlive", true);
  }
  
  /**
   * Start the server on the specified address and port. 
   * @param address The server address.
   * @param port The server port.
   * @return Returns the channel instance of the server socket.
   */
  public Channel start( String address, int port)
  {
    channel = bootstrap.bind( new InetSocketAddress( address, port));
    
    channel.getCloseFuture().addListener( new ChannelFutureListener() {
      public void operationComplete( ChannelFuture future) throws Exception
      {
        reset();
      }
    });
    
    return channel;
  }
  
  /**
   * Stop the server from listening for new connections.
   * @return Returns null or the future that is notified when the channel is closed.
   */
  public ChannelFuture stop()
  {
    return (channel != null)? channel.getCloseFuture(): null;
  }
  
  /**
   * Release resources and prepare this client to make another connection.
   */
  protected void reset()
  {
    // release netty resources
    bootstrap.getFactory().releaseExternalResources();
    
    // release protocol resources
    bind.reset();
    execute.reset();
    
    // prepare for another connection
    handler = new FullProtocolChannelHandler( bind.context, execute.context, execute.scheduler);
  }

  private ServerBootstrap bootstrap;
}
