package org.xmodel.xaction;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmodel.IModelObject;
import org.xmodel.net.IXioPeerRegistry;
import org.xmodel.net.transport.amqp.AmqpServerTransport;
import org.xmodel.net.transport.netty.NettyServerTransport;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

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
     * Start a server on the specified port and return its registry.
     * @param context The execution context.
     * @param onConnect Called when the connection is established.
     * @param onDisconnect Called when the client is disconnected.
     * @param onRegister Called when a peer registers with a specified name.
     * @param onUnregister Called when a peer unregisters.
     */
    public IXioPeerRegistry listen( IContext context, IXAction onConnect, IXAction onDisconnect, IXAction onRegister, IXAction onUnregister)
      throws IOException;
  }
  
  /**
   * Register a new transport.
   * @param name The name of the transport configuration element.
   * @param transport The transport class.
   */
  public static void registerTransport( String name, Class<? extends IServerTransport> transport)
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
    
    var = Conventions.getVarName( document.getRoot(), false);
    
    onConnectExpr = document.getExpression( "onConnect", true);
    onDisconnectExpr = document.getExpression( "onDisconnect", true);
    onRegisterExpr = document.getExpression( "onRegister", true);
    onUnregisterExpr = document.getExpression( "onUnregister", true);
    
    for( IModelObject child: document.getRoot().getChildren())
    {
      Class<?> clss = transports.get( child.getType());
      if ( clss != null)
      {
        try
        {
          transport = (IServerTransport)clss.newInstance(); 
          transport.configure( child);
        }
        catch( Exception e)
        {
          throw new XActionException( "Problem with server transport implementation: ", e);
        }
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
    final IXAction onConnect = (onConnectExpr != null)? getScript( context, onConnectExpr): null;
    final IXAction onDisconnect = (onDisconnectExpr != null)? getScript( context, onDisconnectExpr): null;
    final IXAction onRegister = (onRegisterExpr != null)? getScript( context, onRegisterExpr): null;
    final IXAction onUnregister = (onUnregisterExpr != null)? getScript( context, onUnregisterExpr): null;
    
    try
    {
      IXioPeerRegistry registry = transport.listen( context, onConnect, onDisconnect, onRegister, onUnregister);
      if ( var != null) Conventions.putCache( context, var, registry);
    }
    catch( IOException e)
    {
      throw new XActionException( "Unable to create client: ", e);
    }
    
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
  
  private static Map<String, Class<?>> transports = Collections.synchronizedMap( new HashMap<String, Class<?>>());
  
  static
  {
    registerTransport( "tcp", NettyServerTransport.class);
    registerTransport( "amqp", AmqpServerTransport.class);
  }

  private String var;
  private IExpression onConnectExpr;
  private IExpression onDisconnectExpr;
  private IExpression onRegisterExpr;
  private IExpression onUnregisterExpr;
  private IServerTransport transport;
}
