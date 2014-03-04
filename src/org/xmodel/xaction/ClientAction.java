package org.xmodel.xaction;

import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.net.XioPeer;
import org.xmodel.net.transport.netty.NettyClientTransport;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

/**
 * An XAction that creates a network client using one of the available IPC transports.  
 * Examples of ICP transports include TCP and AMPQ.
 */
public class ClientAction extends GuardedAction
{
  public static interface IClientTransport
  {
    /**
     * Configure from the specified element.
     * @param config The configuration element.
     */
    public void configure( IModelObject config);
    
    /**
     * Run the action implementation.
     * @param context The execution context.
     */
    public XioPeer run( IContext context, String clientName, IXAction onConnect, IXAction onDisconnect, IXAction onError);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    var = Conventions.getVarName( document.getRoot(), true);
    clientNameExpr = document.getExpression( "subscribe", true);
    onConnectExpr = document.getExpression( "onConnect", true);
    onDisconnectExpr = document.getExpression( "onDisconnect", true);
    onErrorExpr = document.getExpression( "onError", true);
    
    IModelObject transport = document.getRoot().getFirstChild( "tcp");
    if ( transport == null) transport = document.getRoot().getFirstChild( "ampq");
    
    createTransport( transport);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( final IContext context)
  {
    String clientName = clientNameExpr.evaluateString( context);
    
    IXAction onConnect = (onConnectExpr != null)? getScript( context, onConnectExpr): null;
    IXAction onDisconnect = (onDisconnectExpr != null)? getScript( context, onDisconnectExpr): null;
    IXAction onError = (onErrorExpr != null)? getScript( context, onErrorExpr): null;
    
    XioPeer client = transport.run( context, clientName, onConnect, onDisconnect, onError);
    
    Conventions.putCache( context, var, client);
    
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
   * Create transport.
   * @param config The configuration element.
   * @return Returns null or the transport.
   */
  private IClientTransport createTransport( IModelObject config)
  {
    switch( config.getType())
    {
      case "tcp": return new NettyClientTransport();
      case "ampq":
      default: return null;
    }
  }
  
  private String var;
  private IExpression clientNameExpr;
  private IExpression onConnectExpr;
  private IExpression onDisconnectExpr;
  private IExpression onErrorExpr;
  private IClientTransport transport;
}
