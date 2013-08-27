package org.xmodel.xaction;

import java.net.InetSocketAddress;
import java.security.SecureRandom;
import java.util.List;
import java.util.concurrent.Executors;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import org.jboss.netty.channel.socket.ClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioClientBossPool;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioWorkerPool;
import org.jboss.netty.util.ThreadNameDeterminer;
import org.xmodel.IModelObject;
import org.xmodel.concurrent.ModelThreadFactory;
import org.xmodel.future.AsyncFuture;
import org.xmodel.future.AsyncFuture.IListener;
import org.xmodel.net.OpenTrustStore;
import org.xmodel.net.XioClient;
import org.xmodel.net.XioPeer;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;

/**
 * Create a client connection and register the client with the server with a peer name.
 * Client connections allow the server to use services of the client on demand.
 */
public class ClientAction extends GuardedAction
{
  public final static int defaultRetries = Integer.MAX_VALUE;
  public final static int[] defaultDelays = { 250, 500, 1000, 2000, 3000, 4000, 5000, 5000, 5000, 5000, 10000};
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    var = Conventions.getVarName( document.getRoot(), true); 
    serverHostExpr = document.getExpression( "serverHost", true);
    serverPortExpr = document.getExpression( "serverPort", true);
    clientNameExpr = document.getExpression( "subscribe", true);
    threadsExpr = document.getExpression( "threads", true);
    
    sslExpr = document.getExpression( "ssl", true);
    
    onConnectExpr = document.getExpression( "onConnect", true);
    onDisconnectExpr = document.getExpression( "onDisconnect", true);
    onErrorExpr = document.getExpression( "onError", true);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( final IContext context)
  {
    final String serverHost = serverHostExpr.evaluateString( context);
    final int serverPort = (int)serverPortExpr.evaluateNumber( context);
    final String clientName = clientNameExpr.evaluateString( context);
    final IXAction onConnect = (onConnectExpr != null)? getScript( context, onConnectExpr): null;
    final IXAction onDisconnect = (onDisconnectExpr != null)? getScript( context, onDisconnectExpr): null;
    final IXAction onError = (onErrorExpr != null)? getScript( context, onErrorExpr): null;
    
    class ClientListener implements XioClient.IListener
    {
      public void notifyConnect( final XioClient client)
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
      public void notifyDisconnect( final XioClient client)
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
    
    ModelThreadFactory bossThreadFactory = new ModelThreadFactory( String.format( "xio-client-boss-%s:%d", serverHost, serverPort));
    ModelThreadFactory workThreadFactory = new ModelThreadFactory( String.format( "xio-client-work-%s:%d", serverHost, serverPort));
    
    NioClientBossPool bossPool = new NioClientBossPool( Executors.newCachedThreadPool( bossThreadFactory), 1, XioPeer.timer, ThreadNameDeterminer.CURRENT);
    NioWorkerPool workerPool = new NioWorkerPool( Executors.newCachedThreadPool( workThreadFactory), threads, ThreadNameDeterminer.CURRENT);
    ClientSocketChannelFactory channelFactory = new NioClientSocketChannelFactory( bossPool, workerPool);
    
    XioClient client = new XioClient( context, null, channelFactory, getSSLContext( context), context.getExecutor());
    client.setAutoReconnect( true);
    client.addListener( new ClientListener());
    
    AsyncFuture<XioClient> future = client.connect( serverHost, serverPort, defaultRetries, defaultDelays);
    future.addListener( new IListener<XioClient>() {
      public void notifyComplete( AsyncFuture<XioClient> future) throws Exception
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
    
    Conventions.putCache( context, var, client);
    
    return null;
  }
  
  /**
   * Register the client.
   * @param context The context.
   * @param client The client.
   * @param name The client registration name.
   * @param onError The error handler.
   */
  private void register( final IContext context, XioClient client, String name, final IXAction onError)
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
   * Get the script from the specified expression.
   * @param context The context.
   * @param expression The script expression.
   * @return Returns null or the script.
   */
  private IXAction getScript( IContext context, IExpression expression)
  {
    if ( expression == null) return null;
    List<IModelObject> elements = expression.evaluateNodes( context);
    return new ScriptAction( elements);
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
  
  private String var;
  private IExpression serverHostExpr;
  private IExpression serverPortExpr;
  private IExpression clientNameExpr;
  private IExpression threadsExpr;
  private IExpression sslExpr;
  private IExpression onConnectExpr;
  private IExpression onDisconnectExpr;
  private IExpression onErrorExpr;
}
