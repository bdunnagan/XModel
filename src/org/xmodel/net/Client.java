package org.xmodel.net;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.xmodel.GlobalSettings;
import org.xmodel.xpath.expression.IContext;

/**
 * This class provides an interface for the client-side of the protocol.
 */
public class Client extends Peer
{
  public Client()
  {
    this( null, null, GlobalSettings.getInstance().getScheduler(), Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
  }
  
  /**
   * Create a client that uses an NioClientSocketChannelFactory configured with tcp-no-delay and keep-alive.
   * @param scheduler The scheduler used for protocol timers.
   * @param bossExecutor The NioClientSocketChannelFactory boss executor.
   * @param workerExecutor The NioClientSocketChannelFactory worker executor.
   */
  public Client( ScheduledExecutorService scheduler, Executor bossExecutor, Executor workerExecutor)
  {
    this( null, null, scheduler, bossExecutor, workerExecutor);
  }
  
  /**
   * Create a client that uses an NioClientSocketChannelFactory configured with tcp-no-delay and keep-alive.
   * @param bindContext The context for the remote bind protocol.
   * @param executeContext The context for the remote execution protocol.
   * @param scheduler The scheduler used for protocol timers.
   * @param bossExecutor The NioClientSocketChannelFactory boss executor.
   * @param workerExecutor The NioClientSocketChannelFactory worker executor.
   */
  public Client( IContext bindContext, IContext executeContext, ScheduledExecutorService scheduler, Executor bossExecutor, Executor workerExecutor)
  {
    super( bindContext, executeContext, scheduler);
    
    bootstrap = new ClientBootstrap( new NioClientSocketChannelFactory( bossExecutor, workerExecutor));
    bootstrap.setPipelineFactory( this);
    bootstrap.setOption( "tcpNoDelay", true);
    bootstrap.setOption( "keepAlive", true);
    
    channel.getCloseFuture().addListener( new ChannelFutureListener() {
      public void operationComplete( ChannelFuture future) throws Exception
      {
        reset();
      }
    });
    
    connected = new AtomicBoolean( false);
  }
  
  /**
   * Connect this client to the specified server. A client may only be connected to one server.
   * @param host The server host.
   * @param port The server port.
   * @return Returns true if the connection was established.
   */
  public ChannelFuture connect( String host, int port)
  {
    if ( connected.getAndSet( true)) throw new IllegalStateException( "Client is already connected.");
    return bootstrap.connect( new InetSocketAddress( host, port));  
  }
  
  /**
   * Close the connection that was previously established with a call to the <code>connect</code> method.
   * If this client is to be reused to make another connection, the connection must not be attempted 
   * until the ChannelFuture returned by this method provides notification.
   * @return Returns null or the future that is notified when the channel is closed.
   */
  public ChannelFuture close()
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

  private ClientBootstrap bootstrap;
  private AtomicBoolean connected;
}
