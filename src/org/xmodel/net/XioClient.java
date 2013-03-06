package org.xmodel.net;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.ssl.SslHandler;
import org.xmodel.GlobalSettings;
import org.xmodel.concurrent.ModelThreadFactory;
import org.xmodel.future.AsyncFuture;
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
    this( null, null, executor);
  }
  
  /**
   * Create a client that uses an NioClientSocketChannelFactory configured with tcp-no-delay and keep-alive.
   * The executors for both protocols use GlobalSettings.getInstance().getModel().getExecutor() of this thread.
   * @param context The context.
   */
  public XioClient( IContext context)
  {
    this( context, null, context.getExecutor());
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
   * @param contextExecutor
   */
  public XioClient( final IContext context, final ScheduledExecutorService scheduler, final Executor contextExecutor)
  {
    this( context, scheduler, getDefaultChannelFactory(), contextExecutor);
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
   * @param channelFactory User-supplied channel factory.
   * @param contextExecutor
   */
  public XioClient( final IContext context, final ScheduledExecutorService scheduler, NioClientSocketChannelFactory channelFactory, final Executor contextExecutor)
  {
    this( context, scheduler, channelFactory, null, contextExecutor);
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
   * @param channelFactory User-supplied channel factory.
   * @param sslContext An SSLContext.
   * @param contextExecutor
   */
  public XioClient( 
      final IContext context, 
      final ScheduledExecutorService scheduler, 
      final NioClientSocketChannelFactory channelFactory, 
      final SSLContext sslContext, 
      final Executor contextExecutor)
  {
    this.scheduler = (scheduler != null)? scheduler: GlobalSettings.getInstance().getScheduler();
    
    bootstrap = new ClientBootstrap( channelFactory);
    bootstrap.setOption( "tcpNoDelay", true);
    bootstrap.setOption( "keepAlive", true);
    
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
        
        pipeline.addLast( "xio", new XioChannelHandler( context, contextExecutor, scheduler, null));
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
  public AsyncFuture<XioClient> connect( String address, int port)
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
  public AsyncFuture<XioClient> connect( String address, int port, int retries, int delay)
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
  public AsyncFuture<XioClient> connect( String address, int port, int[] delays)
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
  public AsyncFuture<XioClient> connect( String address, int port, int retries, int[] delays)
  {
    return connect( new InetSocketAddress( address, port), retries, delays);
  }
  
  /**
   * Connect this client to the specified server. A client may only be connected to one server.
   * @param address The address of the server.
   * @return Returns true if the connection was established.
   */
  public AsyncFuture<XioClient> connect( InetSocketAddress address)
  {
    return connect( address, 3, 1000);
  }
  
  /**
   * Attempt to connect this client to the specified server the specified number of times.
   * @param address The address of the server.
   * @param port The port of the server.
   * @param retries The maximum number of retries.
   * @param delay The delay between retries in milliseconds.
   * @return Returns a future that is retry-aware.
   */
  public AsyncFuture<XioClient> connect( InetSocketAddress address, int retries, int delay)
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
  public AsyncFuture<XioClient> connect( InetSocketAddress address, int[] delays)
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
  public AsyncFuture<XioClient> connect( InetSocketAddress address, int retries, int[] delays)
  {
    synchronized( this)
    {
      lastAddress = address;
      lastRetries = retries;
      lastDelays = delays;
    }
    
    final ConnectionRetryFuture retryFuture = new ConnectionRetryFuture( bootstrap, address, scheduler, retries, delays);
    
    final AsyncFuture<XioClient> asyncFuture = new AsyncFuture<XioClient>( this) {
      public void cancel()
      {
        retryFuture.cancel();
      }
    };
    
    retryFuture.addListener( new ChannelFutureListener() {
      public void operationComplete( ChannelFuture retryFuture) throws Exception
      {
        if ( retryFuture.isSuccess()) 
        {
          setChannel( retryFuture.getChannel());
          
          XioChannelHandler xioHandler = retryFuture.getChannel().getPipeline().get( XioChannelHandler.class);
          ChannelFuture handshakeFuture = xioHandler.getSSLHandshakeFuture();
          if ( handshakeFuture != null)
          {
            handshakeFuture.addListener( new ChannelFutureListener() {
              public void operationComplete( ChannelFuture handshakeFuture) throws Exception
              {
                if ( handshakeFuture.isSuccess()) 
                {
                  asyncFuture.notifySuccess();
                }
                else
                {
                  asyncFuture.notifyFailure( handshakeFuture.getCause());
                }
              }
            });
          }
          else
          {
            asyncFuture.notifySuccess();
          }
        }
        else
        {
          asyncFuture.notifyFailure( retryFuture.getCause());
        }
      }
    });
    
    return asyncFuture;
  }
    
  /* (non-Javadoc)
   * @see org.xmodel.net.XioPeer#reconnect()
   */
  @Override
  protected AsyncFuture<XioClient> reconnect()
  {
    InetSocketAddress address = null;
    int retries = 0;
    int[] delays = null;
    
    synchronized( this)
    {
      address = lastAddress;
      retries = lastRetries;
      delays = lastDelays;
    }
    
    return (lastAddress != null)? connect( address, retries, delays): null;
  }

  private static synchronized NioClientSocketChannelFactory getDefaultChannelFactory()
  {
    if ( defaultChannelFactory == null)
      defaultChannelFactory = new NioClientSocketChannelFactory( getDefaultBossExecutor(), getDefaultWorkerExecutor());
    return defaultChannelFactory;
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
  
  private static NioClientSocketChannelFactory defaultChannelFactory = null;
  private static Executor defaultBossExecutor = null;
  private static Executor defaultWorkerExecutor = null;
  
  private ClientBootstrap bootstrap;
  private ScheduledExecutorService scheduler;
  private InetSocketAddress lastAddress;
  private int lastRetries;
  private int[] lastDelays;
}
