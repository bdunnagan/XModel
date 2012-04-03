package org.xmodel.xaction;

import java.io.IOException;
import org.xmodel.IModelObject;
import org.xmodel.net.Client;
import org.xmodel.net.Session;
import org.xmodel.xaction.debug.Debugger;
import org.xmodel.xaction.debug.Debugger.Operation;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.variable.IVariableScope;

/**
 * An XAction that performs debugging operations.
 */
public class DebugAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    hostExpr = document.getExpression( "host", true);
    portExpr = document.getExpression( "port", true);
    timeoutExpr = document.getExpression( "timeout", true);
    
    opExpr = document.getExpression( "op", true);
    
    var = Conventions.getVarName( document.getRoot(), false);
    
    if ( sessions == null) sessions = new ThreadLocal<Session>();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    String op = opExpr.evaluateString( context);
    op = op.trim().toLowerCase();
    
    if ( sessions.get() == null)
    {
      String host = (hostExpr != null)? hostExpr.evaluateString( context): "127.0.0.1";
      int port = (portExpr != null)? (int)portExpr.evaluateNumber( context): Debugger.defaultPort;
      int timeout = (timeoutExpr != null)? (int)timeoutExpr.evaluateNumber( context): Integer.MAX_VALUE;
      try
      {
        Client client = new Client( host, port, timeout, true);
        Session session = client.connect( timeout);
        sessions.set( session);
      } 
      catch( IOException e)
      {
        throw new XActionException( String.format( "Unable to connect to %s:%d.", host, port), e);
      }
    }
    
    double timeout = (timeoutExpr != null)? timeoutExpr.evaluateNumber( context): 10000;
    
    try
    {
      Session session = sessions.get();
      IModelObject response = session.debug( Operation.valueOf( op), (int)timeout);
      
      if ( var != null)
      {
        IVariableScope scope = context.getScope();
        if ( scope != null) scope.set( var, response);
      }
    }
    catch( IOException e)
    {
      throw new XActionException( "Unable to perform debug operation.", e);
    }
    
    return null;
  }
  
  private static ThreadLocal<Session> sessions;
  
  private IExpression hostExpr;
  private IExpression portExpr;
  private IExpression timeoutExpr;
  private IExpression opExpr;
  private String var;
}
