package org.xmodel.xaction;

import java.net.InetSocketAddress;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.net.ssl.SSLContext;
import org.xmodel.IModelObject;
import org.xmodel.net.XioPeer;
import org.xmodel.net.XioServer;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;

/**
 * An XAction that creates an XIO server.
 */
public class ServerAction extends GuardedAction
{
  public ServerAction()
  {
    notifyContexts = new HashMap<XioPeer, StatefulContext>();
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
    
    final XioServer server = new XioServer( getSSLContext( context), context);
    
    if ( onConnect != null || onDisconnect != null || onRegister != null || onUnregister != null)
    {
      XioServer.IListener listener = new XioServer.IListener() {
        public void notifyConnect( final XioPeer peer)
        {
          if ( onConnect != null) 
          {
            context.getExecutor().execute( new Runnable() {
              public void run() 
              {
                StatefulContext nested = getNotifyContext( context, server, peer);
                onConnect.run( nested);
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
                StatefulContext nested = getNotifyContext( context, server, peer);
                if ( nested != null) onDisconnect.run( nested);
              }
            });
          }
        }
        public void notifyRegister( final XioPeer peer, final String name)
        {
          if ( onRegister != null) 
          {
            if ( name.equals( getIdentityRegistration( peer)))
              return;
            
            context.getExecutor().execute( new Runnable() {
              public void run() 
              {
                StatefulContext nested = getNotifyContext( context, server, peer);
                if ( nested != null)
                {
                  nested.set( "name", (name != null)? name: "");
                  onRegister.run( nested);
                }
              }
            });
          }
        }
        public void notifyUnregister( final XioPeer peer, final String name)
        {
          if ( name.equals( getIdentityRegistration( peer)))
            return;
          
          if ( onUnregister != null) 
          {
            context.getExecutor().execute( new Runnable() {
              public void run() 
              {
                StatefulContext nested = getNotifyContext( context, server, peer);
                if ( nested != null) 
                {
                  nested.set( "name", (name != null)? name: "");
                  onUnregister.run( nested);
                }
              }
            });
          }
        }
      };
      
      server.addListener( listener);
    }
    
    // servers that support client registration must be accessible
    if ( var != null) Conventions.putCache( context, var, server);
    
    // bind server
    String address = addressExpr.evaluateString( context);
    int port = (int)portExpr.evaluateNumber( context);
    server.start( address, port);
    
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
  
  private StatefulContext getNotifyContext( IContext context, XioServer server, XioPeer peer)
  {
    synchronized( notifyContexts)
    {
      StatefulContext nested = notifyContexts.get( peer);
      if ( nested == null)
      {
        nested = new StatefulContext( context);
        notifyContexts.put( peer, nested);
        
        InetSocketAddress address = peer.getRemoteAddress();
        nested.set( "address", String.format( "%s:%d", address.getAddress().getHostAddress(), address.getPort()));

        String unique = getIdentityRegistration( peer);
        server.getPeerRegistry().register( peer, unique);
        nested.set( "peer", unique);
      }
      return nested;
    }
  }
  
  private String getIdentityRegistration( XioPeer peer)
  {
    return String.format( "%X", System.identityHashCode( peer));
  }
  
  private String var;
  
  private IExpression addressExpr;
  private IExpression portExpr;
  private IExpression sslExpr;
  private IExpression onConnectExpr;
  private IExpression onDisconnectExpr;
  private IExpression onRegisterExpr;
  private IExpression onUnregisterExpr;
  private Map<XioPeer, StatefulContext> notifyContexts;
}
