package org.xmodel.xaction;

import java.net.InetSocketAddress;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.Executors;
import javax.net.ssl.SSLContext;
import org.jboss.netty.channel.socket.ServerSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerBossPool;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioWorkerPool;
import org.jboss.netty.util.ThreadNameDeterminer;
import org.xmodel.IModelObject;
import org.xmodel.net.XioPeer;
import org.xmodel.net.transport.netty.NettyXioServer;
import org.xmodel.util.PrefixThreadFactory;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;

/**
 * An XAction that creates an XIO server.
 */
public class ServerAction extends GuardedAction
{
  public interface IServerTransport
  {
    /**
     * Configure from the specified element.
     * @param config The configuration element.
     */
    public void configure( IModelObject config);
    
    /**
     * Create an XioPeer that uses this transport.
     * @param context The execution context.
     * @param clientName The subscription name for the client.
     * @param onConnect Called when the connection is established.
     * @param onDisconnect Called when the client is disconnected.
     * @param onRegister Called when a peer registers with a specified name.
     * @param onUnregister Called when a peer unregisters.
     */
    public XioPeer connect( IContext context, String clientName, IXAction onConnect, IXAction onDisconnect, IXAction onError, IXAction onRegister, IXAction onUnregister);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    var = Conventions.getVarName( document.getRoot(), false);
    
    addressExpr = document.getExpression( "address", true);
    portExpr = document.getExpression( "port", true);
    sslExpr = document.getExpression( "ssl", true);
    threadsExpr = document.getExpression( "threads", true);
    
    onConnectExpr = document.getExpression( "onConnect", true);
    onDisconnectExpr = document.getExpression( "onDisconnect", true);
    onRegisterExpr = document.getExpression( "onRegister", true);
    onUnregisterExpr = document.getExpression( "onUnregister", true);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( final IContext context)
  {
    final IXAction onConnect = (onConnectExpr != null)? getScript( context, onConnectExpr): null;
    final IXAction onDisconnect = (onDisconnectExpr != null)? getScript( context, onDisconnectExpr): null;
    final IXAction onRegister = (onRegisterExpr != null)? getScript( context, onRegisterExpr): null;
    final IXAction onUnregister = (onUnregisterExpr != null)? getScript( context, onUnregisterExpr): null;
    
    
    // servers that support client registration must be accessible
    if ( var != null) Conventions.putCache( context, var, server);
    
    return null;
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
  
  private String var;
  
  private IExpression addressExpr;
  private IExpression portExpr;
  private IExpression sslExpr;
  private IExpression threadsExpr;
  private IExpression onConnectExpr;
  private IExpression onDisconnectExpr;
  private IExpression onRegisterExpr;
  private IExpression onUnregisterExpr;
}
