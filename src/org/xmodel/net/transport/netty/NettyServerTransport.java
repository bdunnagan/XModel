package org.xmodel.net.transport.netty;

import java.net.InetSocketAddress;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;

import org.jboss.netty.channel.socket.ServerSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerBossPool;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioWorkerPool;
import org.jboss.netty.util.ThreadNameDeterminer;
import org.xmodel.IModelObject;
import org.xmodel.Xlate;
import org.xmodel.net.IXioPeerRegistry;
import org.xmodel.net.XioPeer;
import org.xmodel.util.PrefixThreadFactory;
import org.xmodel.xaction.IXAction;
import org.xmodel.xaction.ServerAction.IServerTransport;
import org.xmodel.xaction.XActionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;

public class NettyServerTransport implements IServerTransport
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.ServerAction.IServerTransport#configure(org.xmodel.IModelObject)
   */
  @Override
  public void configure( IModelObject config)
  {
    addressExpr = Xlate.get( config, "address", Xlate.childGet( config, "address", (IExpression)null));
    portExpr = Xlate.get( config, "port", Xlate.childGet( config, "port", (IExpression)null));
    sslExpr = Xlate.get( config, "ssl", Xlate.childGet( config, "ssl", (IExpression)null));
    threadsExpr = Xlate.get( config, "threads", Xlate.childGet( config, "threads", (IExpression)null));
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.ServerAction.IServerTransport#listen(org.xmodel.xpath.expression.IContext, org.xmodel.xaction.IXAction, 
   * org.xmodel.xaction.IXAction, org.xmodel.xaction.IXAction, org.xmodel.xaction.IXAction)
   */
  @Override
  public IXioPeerRegistry listen( 
      final IContext context, 
      final IXAction onConnect, 
      final IXAction onDisconnect,
      final IXAction onRegister, 
      final IXAction onUnregister)
  {
    String address = addressExpr.evaluateString( context);
    int port = (int)portExpr.evaluateNumber( context);
    int threads = (threadsExpr != null)? (int)threadsExpr.evaluateNumber( context): 0;
    if ( threads == 0) threads = 2;
    
    PrefixThreadFactory bossThreadFactory = new PrefixThreadFactory( String.format( "xio-server-boss-%s:%d", address, port));
    PrefixThreadFactory workThreadFactory = new PrefixThreadFactory( String.format( "xio-server-work-%s:%d", address, port));
    
    NioServerBossPool bossPool = new NioServerBossPool( Executors.newCachedThreadPool( bossThreadFactory), 1, ThreadNameDeterminer.CURRENT);
    NioWorkerPool workerPool = new NioWorkerPool( Executors.newCachedThreadPool( workThreadFactory), threads, ThreadNameDeterminer.CURRENT);
    ServerSocketChannelFactory channelFactory = new NioServerSocketChannelFactory( bossPool, workerPool);
    
    final NettyXioServer server = new NettyXioServer( getSSLContext( context), context, null, channelFactory);
    
    if ( onConnect != null || onDisconnect != null || onRegister != null || onUnregister != null)
    {
      NettyXioServer.IListener listener = new NettyXioServer.IListener() {
        public void notifyConnect( final XioPeer peer)
        {
          if ( onConnect != null) 
          {
            context.getExecutor().execute( new Runnable() {
              public void run() 
              {
                configureEventContext( peer);
                onConnect.run( peer.getNetworkEventContext());
              }
            });
          }
        }
        public void notifyDisconnect( final XioPeer peer)
        {
          if ( onDisconnect != null) 
          {
            context.getExecutor().execute( new Runnable() {
              public void run() 
              {
                StatefulContext eventContext = peer.getNetworkEventContext(); 
                if ( eventContext != null) onDisconnect.run( eventContext);
              }
            });
          }
        }
        public void notifyRegister( final XioPeer peer, final String name)
        {
          if ( onRegister != null) 
          {
            context.getExecutor().execute( new Runnable() {
              public void run() 
              {
                StatefulContext eventContext = peer.getNetworkEventContext(); 
                if ( eventContext != null)
                {
                  eventContext.set( "name", (name != null)? name: "");
                  onRegister.run( eventContext);
                }
              }
            });
          }
        }
        public void notifyUnregister( final XioPeer peer, final String name)
        {
          if ( onUnregister != null) 
          {
            context.getExecutor().execute( new Runnable() {
              public void run() 
              {
                StatefulContext eventContext = peer.getNetworkEventContext(); 
                if ( eventContext != null)
                {
                  eventContext.set( "name", (name != null)? name: "");
                  onUnregister.run( eventContext);
                }
              }
            });
          }
        }
      };
      
      server.addListener( listener);
    }
    
    // bind server
    server.start( address, port);
    
    return server.getPeerRegistry();
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
      return SSLContext.getDefault();
    }
    catch( NoSuchAlgorithmException e)
    {
      throw new XActionException( e);
    }
  }

  /**
   * Set default variables of new event context.
   * @param context The event context.
   */
  private void configureEventContext( XioPeer peer)
  {
    StatefulContext context = peer.getNetworkEventContext();
    
    InetSocketAddress localAddress = peer.getLocalAddress();
    if ( localAddress != null) context.set( "localAddress", String.format( "%s:%d", localAddress.getAddress().getHostAddress(), localAddress.getPort()));
    
    InetSocketAddress remoteAddress = peer.getRemoteAddress();
    if ( remoteAddress != null) context.set( "remoteAddress", String.format( "%s:%d", remoteAddress.getAddress().getHostAddress(), remoteAddress.getPort()));
  }
  
  private IExpression addressExpr;
  private IExpression portExpr;
  private IExpression sslExpr;
  private IExpression threadsExpr;
}
