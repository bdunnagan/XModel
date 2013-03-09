package org.xmodel.net;

import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.ssl.SslHandler;
import org.jboss.netty.util.ThreadNameDeterminer;
import org.jboss.netty.util.ThreadRenamingRunnable;
import org.xmodel.concurrent.ModelThreadFactory;
import org.xmodel.future.AsyncFuture;
import org.xmodel.net.execution.ExecutionPrivilege;
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
    this( null, context, null, null, null);
  }
  
  /**
   * Create a client that uses an NioClientSocketChannelFactory configured with tcp-no-delay and keep-alive.
   * The executors for both protocols use GlobalSettings.getInstance().getModel().getExecutor() of this thread.
   * @param SSLContext An SSLContext.
   * @param context The context.
   */
  public XioServer( SSLContext sslContext, IContext context)
  {
    this( sslContext, context, null, null, null);
  }
  
  /**
   * Create a server that uses an NioServerSocketChannelFactory configured with tcp-no-delay and keep-alive. Null may be passed
   * for optional arguments. The following table shows defaults used for each optional argument:
   * <ul>
   *   <li>context - Required for remote execution from the server</li>
   *   <li>scheduler - GlobalSettings.getInstance().getScheduler()</li>
   *   <li>bossExecutor - Static ExecutorService.newCachedThreadPool</li>
   *   <li>workerExecutor - Static ExecutorService.newCachedThreadPool</li>
   * </ul>
   * @param SSLContext Optional SSLContext.
   * @param context Optional context.
   * @param scheduler Optional scheduler used for protocol timers.
   * @param bossExecutor Optional NioClientSocketChannelFactory boss executor.
   * @param workerExecutor Optional oClientSocketChannelFactory worker executor.
   */
  public XioServer( final SSLContext sslContext, final IContext context, final ScheduledExecutorService scheduler, Executor bossExecutor, Executor workerExecutor)
  {
    this.registry = new MemoryXioPeerRegistry( this);
    
    ThreadRenamingRunnable.setThreadNameDeterminer( ThreadNameDeterminer.CURRENT);    
    
    if ( bossExecutor == null) bossExecutor = getDefaultBossExecutor();
    if ( workerExecutor == null) workerExecutor = getDefaultWorkerExecutor();
    
    NioServerSocketChannelFactory channelFactory = new NioServerSocketChannelFactory( bossExecutor, workerExecutor);
    bootstrap = new ServerBootstrap( channelFactory);
    bootstrap.setOption( "child.tcpNoDelay", true);
    bootstrap.setOption( "child.keepAlive", true);
    
    bootstrap.setPipelineFactory( new ChannelPipelineFactory() {
      public ChannelPipeline getPipeline() throws Exception
      {
        ChannelPipeline pipeline = Channels.pipeline();
        
        if ( sslContext != null)
        {
          SSLEngine engine = sslContext.createSSLEngine();
          engine.setUseClientMode( false);
          pipeline.addLast( "ssl", new SslHandler( engine));
        }
        
        XioChannelHandler channelHandler = new XioChannelHandler( context, context.getExecutor(), scheduler, registry);
        channelHandler.getExecuteProtocol().requestProtocol.setPrivilege( executionPrivilege);
        pipeline.addLast( "xio", channelHandler);
        return pipeline;
      }
    });
  }
  
  /**
   * Set the execution privileges.
   * @param privilege The privilege instance.
   */
  public void setExecutionPrivileges( ExecutionPrivilege privilege)
  {
    this.executionPrivilege = privilege;
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
      serverChannel.close().addListener( new ChannelFutureListener() {
        public void operationComplete( ChannelFuture future) throws Exception
        {
          serverChannel.getFactory().releaseExternalResources();
        }
      });
    }
  }
  
  /**
   * Register the specified remote-host with the specified name.
   * @param name A name, not necessarily unique, to associate with the peer.
   * @param host The host to be registered.
   * @param ttl The duration of the association in milliseconds.
   */
  public void register( String name, String host, long ttl)
  {
    registry.register( name, host);
  }
  
  /**
   * Cancel a peer registration by name and host.
   * @param name The name associated with the peer.
   * @param host The remote host.
   */
  public void cancel( String name, String host)
  {
    registry.unregister( name, host);
  }

  /**
   * Returns a future for obtaining a peer connection from the specified host.
   * @param host The remote host.
   * @return Returns a future for a peer connection from the specified host.
   */
  public AsyncFuture<XioPeer> getPeerByHost( String host)
  {
    return registry.lookupByHost( host);
  }
  
  /**
   * Returns an iterator over XioPeer instances registered under the specified name.
   * @param name The name.
   * @return Returns the associated peers.
   */
  public Iterator<XioPeer> getPeersByName( String name)
  {
    return registry.lookupByName( name);
  }

  /**
   * Add a peer registration listener.
   * @param listener The The listener.
   */
  public void addPeerRegistryListener( IXioPeerRegistryListener listener)
  {
    registry.addListener( listener);
  }
  
  /**
   * Remove a peer registration listener.
   * @param listener The listener.
   */
  public void removePeerRegistryListener( IXioPeerRegistryListener listener)
  {
    registry.removeListener( listener);
  }
  
  private static synchronized Executor getDefaultBossExecutor()
  {
    if ( defaultBossExecutor == null)
      defaultBossExecutor = Executors.newCachedThreadPool( new ModelThreadFactory( "xio-server-boss"));
    return defaultBossExecutor;
  }
  
  private static synchronized Executor getDefaultWorkerExecutor()
  {
    if ( defaultWorkerExecutor == null)
      defaultWorkerExecutor = Executors.newCachedThreadPool( new ModelThreadFactory( "xio-server-work"));
    return defaultWorkerExecutor;
  }
  
  private static Executor defaultBossExecutor = null;
  private static Executor defaultWorkerExecutor = null;
  
  private ServerBootstrap bootstrap;
  private Channel serverChannel;
  private IXioPeerRegistry registry;
  private ExecutionPrivilege executionPrivilege;
}
