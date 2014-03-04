package org.xmodel.net.transport.netty;

import java.net.InetSocketAddress;
import java.security.SecureRandom;
import java.util.concurrent.Executors;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import org.jboss.netty.channel.socket.ClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioClientBossPool;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioWorkerPool;
import org.jboss.netty.util.ThreadNameDeterminer;
import org.xmodel.IModelObject;
import org.xmodel.Xlate;
import org.xmodel.future.AsyncFuture;
import org.xmodel.future.AsyncFuture.IListener;
import org.xmodel.net.OpenTrustStore;
import org.xmodel.net.XioPeer;
import org.xmodel.util.PrefixThreadFactory;
import org.xmodel.xaction.ClientAction.IClientTransport;
import org.xmodel.xaction.IXAction;
import org.xmodel.xaction.XActionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;

/**
 * Create a client connection and register the client with the server with a peer name.
 * Client connections allow the server to use services of the client on demand.
 */
public class NettyClientTransport implements IClientTransport
{
  public final static int defaultRetries = Integer.MAX_VALUE;
  public final static int[] defaultDelays = { 250, 500, 1000, 2000, 3000, 4000, 5000, 5000, 5000, 5000, 10000};
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.ClientAction.IClientActionImpl#configure(org.xmodel.IModelObject)
   */
  @Override
  public void configure( IModelObject config)
  {
    serverHostExpr = Xlate.get( config, "serverHost", Xlate.childGet( config, "serverHost", (IExpression)null));
    serverPortExpr = Xlate.get( config, "serverPort", Xlate.childGet( config, "serverPort", (IExpression)null));
    localAddressExpr = Xlate.get( config, "localAddress", Xlate.childGet( config, "localAddress", (IExpression)null));
    localPortExpr = Xlate.get( config, "localPort", Xlate.childGet( config, "localPort", (IExpression)null));
    sslExpr = Xlate.get( config, "ssl", Xlate.childGet( config, "ssl", (IExpression)null));
    threadsExpr = Xlate.get( config, "threads", Xlate.childGet( config, "threads", (IExpression)null));
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.ClientAction.IClientActionImpl#run(org.xmodel.xpath.expression.IContext, java.lang.String, org.xmodel.xaction.IXAction, org.xmodel.xaction.IXAction, org.xmodel.xaction.IXAction)
   */
  @Override
  public XioPeer run( final IContext context, final String clientName, final IXAction onConnect, final IXAction onDisconnect, final IXAction onError)
  {
    final String serverHost = serverHostExpr.evaluateString( context);
    final int serverPort = (int)serverPortExpr.evaluateNumber( context);
    final String localAddress = (localAddressExpr != null)? localAddressExpr.evaluateString( context): "";
    final int localPort = (localPortExpr != null)? (int)localPortExpr.evaluateNumber( context): 0;
    
    class ClientListener implements NettyXioClient.IListener
    {
      public void notifyConnect( final NettyXioClient client)
      {
        nested = new StatefulContext( context);
        
        InetSocketAddress localAddress = client.getLocalAddress();
        nested.set( "localAddress", String.format( "%s:%d", localAddress.getAddress().getHostAddress(), localAddress.getPort()));
        
        InetSocketAddress remoteAddress = client.getRemoteAddress();
        nested.set( "remoteAddress", String.format( "%s:%d", remoteAddress.getAddress().getHostAddress(), remoteAddress.getPort()));
        
        context.getExecutor().execute( new Runnable() {
          public void run()
          {
            if ( onConnect != null) onConnect.run( nested);
            register( nested, client, clientName, onError);
          }
        });
      }
      public void notifyDisconnect( final NettyXioClient client)
      {
        context.getExecutor().execute( new Runnable() {
          public void run()
          {
            if ( onDisconnect != null) onDisconnect.run( nested);
          }
        });
      }
      
      private StatefulContext nested;
    };
    
    int threads = (threadsExpr != null)? (int)threadsExpr.evaluateNumber( context): 1;
    
    PrefixThreadFactory bossThreadFactory = new PrefixThreadFactory( String.format( "xio-client-boss-%s:%d", serverHost, serverPort));
    PrefixThreadFactory workThreadFactory = new PrefixThreadFactory( String.format( "xio-client-work-%s:%d", serverHost, serverPort));
    
    NioClientBossPool bossPool = new NioClientBossPool( Executors.newCachedThreadPool( bossThreadFactory), 1, XioPeer.timer, ThreadNameDeterminer.CURRENT);
    NioWorkerPool workerPool = new NioWorkerPool( Executors.newCachedThreadPool( workThreadFactory), threads, ThreadNameDeterminer.CURRENT);
    ClientSocketChannelFactory channelFactory = new NioClientSocketChannelFactory( bossPool, workerPool);
    
    NettyXioClient client = new NettyXioClient( context, null, channelFactory, getSSLContext( context), context.getExecutor());
    client.setAutoReconnect( true);
    client.addListener( new ClientListener());
    
    if ( localAddress.length() > 0) client.bind( localAddress, localPort);
    
    AsyncFuture<NettyXioClient> future = client.connect( serverHost, serverPort, defaultRetries, defaultDelays);
    future.addListener( new IListener<NettyXioClient>() {
      public void notifyComplete( AsyncFuture<NettyXioClient> future) throws Exception
      {
        if ( !future.isSuccess())
        {
          context.getExecutor().execute( new Runnable() {
            public void run()
            {
              if ( onError != null) onError.run( context);
            }
          });
        }
      }
    });
    
    return client;
  }
  
  /**
   * Register the client.
   * @param context The context.
   * @param client The client.
   * @param name The client registration name.
   * @param onError The error handler.
   */
  private void register( final IContext context, NettyXioClient client, String name, final IXAction onError)
  {
    try
    {
      client.register( name);
    }
    catch( Exception e)
    {
      context.getExecutor().execute( new Runnable() {
        public void run()
        {
          if ( onError != null) onError.run( context);
        }
      });
    }
  }
  
  /**
   * Get the SSLContext if ssl is requested.
   * @param context The context.
   * @return Returns null or the SSLContext.
   */
  private SSLContext getSSLContext( IContext context)
  {
    boolean ssl = (sslExpr != null)? sslExpr.evaluateBoolean( context): false; 
    if ( !ssl) return null;
    
    try
    {
      SSLContext sslContext = SSLContext.getInstance( "TLS");
      sslContext.init( null, new TrustManager[] { new OpenTrustStore()}, new SecureRandom());
      return sslContext;
    }
    catch( Exception e)
    {
      throw new XActionException( e);
    }
  }
  
  private IExpression serverHostExpr;
  private IExpression serverPortExpr;
  private IExpression localAddressExpr;
  private IExpression localPortExpr;
  private IExpression threadsExpr;
  private IExpression sslExpr;
}
