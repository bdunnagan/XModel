package org.xmodel.xaction;

import java.net.InetSocketAddress;
import java.util.List;

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
    
    XioServer.IListener listener = null;
    
    if ( onConnect != null || onDisconnect != null || onRegister != null)
    {
      listener = new XioServer.IListener() {
        public void notifyConnect( XioPeer peer)
        {
          if ( onConnect != null) 
          {
            StatefulContext nested = new StatefulContext( context);
            InetSocketAddress address = peer.getRemoteAddress();
            nested.set( "address", String.format( "%s:%d", address.getAddress().getHostAddress(), address.getPort()));
            onConnect.run( nested);
          }
        }
        public void notifyDisconnect( XioPeer peer)
        {
          if ( onDisconnect != null) 
          {
            StatefulContext nested = new StatefulContext( context);
            
            InetSocketAddress address = peer.getRemoteAddress();
            nested.set( "address", String.format( "%s:%d", address.getAddress().getHostAddress(), address.getPort()));
            
            String name = peer.getRegisteredName();
            nested.set( "name", (name != null)? name: "");
            
            onDisconnect.run( nested);
          }
        }
        public void notifyRegister( XioPeer peer)
        {
          if ( onRegister != null) 
          {
            StatefulContext nested = new StatefulContext( context);
            
            InetSocketAddress address = peer.getRemoteAddress();
            nested.set( "address", String.format( "%s:%d", address.getAddress().getHostAddress(), address.getPort()));
            
            String name = peer.getRegisteredName();
            nested.set( "name", (name != null)? name: "");
            
            onRegister.run( nested);
          }
        }
        public void notifyUnregister( XioPeer peer)
        {
          if ( onUnregister != null) 
          {
            StatefulContext nested = new StatefulContext( context);
            
            InetSocketAddress address = peer.getRemoteAddress();
            nested.set( "address", String.format( "%s:%d", address.getAddress().getHostAddress(), address.getPort()));
            
            String name = peer.getRegisteredName();
            nested.set( "name", (name != null)? name: "");
            
            onUnregister.run( nested);
          }
        }
      };
    }
    
    XioServer server = new XioServer( context);
    
    // servers that support client registration must be accessible
    if ( var != null) Conventions.putCache( context, var, server);
    
    // add listener for script notifications
    if ( listener != null) server.addListener( listener);

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
  
  private String var;
  private IExpression addressExpr;
  private IExpression portExpr;
  private IExpression onConnectExpr;
  private IExpression onDisconnectExpr;
  private IExpression onRegisterExpr;
  private IExpression onUnregisterExpr;
}
