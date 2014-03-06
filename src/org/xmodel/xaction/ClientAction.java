package org.xmodel.xaction;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.xmodel.IModelObject;
import org.xmodel.net.XioPeer;
import org.xmodel.net.transport.amqp.AmqpClientTransport;
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
  
  /**
   * Register a new transport.
   * @param name The name of the transport configuration element.
   * @param transport The transport implementation.
   */
  public static void registerTransport( String name, IClientTransport transport)
  {
    transports.put( name, transport);
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
    onRegisterExpr = document.getExpression( "onRegister", true);
    onUnregisterExpr = document.getExpression( "onUnregister", true);
    
    for( IModelObject child: document.getRoot().getChildren())
    {
      IClientTransport transport = transports.get( child.getType());
      if ( transport != null)
      {
        transport.configure( child);
        this.transport = transport;
      }
    }
    
    if ( transport == null) throw new XActionException( "Transport not defined.");
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( final IContext context)
  {
    String clientName = (clientNameExpr != null)? clientNameExpr.evaluateString( context): null;
    
    final IXAction onConnect = (onConnectExpr != null)? getScript( context, onConnectExpr): null;
    final IXAction onDisconnect = (onDisconnectExpr != null)? getScript( context, onDisconnectExpr): null;
    final IXAction onError = (onErrorExpr != null)? getScript( context, onErrorExpr): null;
    final IXAction onRegister = (onRegisterExpr != null)? getScript( context, onRegisterExpr): null;
    final IXAction onUnregister = (onUnregisterExpr != null)? getScript( context, onUnregisterExpr): null;
    
    XioPeer client = transport.connect( context, clientName, onConnect, onDisconnect, onError, onRegister, onUnregister);
    Conventions.putCache( context, var, client);

    if ( clientName != null) register( context, client, clientName, onError);
    
    return null;
  }

  /**
   * Register the client.
   * @param context The context.
   * @param peer The client.
   * @param name The client registration name.
   * @param onError The error handler.
   */
  private void register( final IContext context, XioPeer peer, String name, final IXAction onError)
  {
    try
    {
      peer.register( name);
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
  
  private static Map<String, IClientTransport> transports = Collections.synchronizedMap( new HashMap<String, IClientTransport>());
  
  static
  {
    registerTransport( "tcp", new NettyClientTransport());
    registerTransport( "amqp", new AmqpClientTransport());
  }
  
  private String var;
  private IExpression clientNameExpr;
  private IExpression onConnectExpr;
  private IExpression onDisconnectExpr;
  private IExpression onErrorExpr;
  private IExpression onRegisterExpr;
  private IExpression onUnregisterExpr;
  private IClientTransport transport;
}
