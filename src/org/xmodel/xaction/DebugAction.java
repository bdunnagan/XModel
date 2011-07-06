package org.xmodel.xaction;

import java.io.IOException;

import org.xmodel.net.Client;
import org.xmodel.xaction.debug.Debugger;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

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
    if ( opExpr == null) opExpr = document.getExpression();
    
    if ( clients == null) clients = new ThreadLocal<Client>();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    String op = opExpr.evaluateString( context);
    op = op.trim().toLowerCase();
    
    if ( op.equals( "start"))
    {
      XAction.setDebugger( new Debugger());
    }
    else if ( op.equals( "stop"))
    {
      XAction.setDebugger( null);
    }
    else
    {
      if ( clients.get() == null)
      {
        String host = (hostExpr != null)? hostExpr.evaluateString( context): "127.0.0.1";
        int port = (portExpr != null)? (int)portExpr.evaluateNumber( context): Debugger.defaultPort;
        int timeout = (timeoutExpr != null)? (int)timeoutExpr.evaluateNumber( context): 15000;
        try
        {
          clients.set( new Client( host, port, timeout));
        } 
        catch( IOException e)
        {
          throw new XActionException( String.format( "Unable to connect to %s:%d.", host, port), e);
        }
      }
      
      Client client = clients.get();
      try
      {
        if ( !client.isConnected()) client.connect( 30000);
        
        if ( op.equals( "stepin")) client.sendDebugStepIn();
        else if ( op.equals( "stepover")) client.sendDebugStepOver();
        else if ( op.equals( "stepout")) client.sendDebugStepOut();
        else if ( op.length() == 0) throw new XActionException( "Empty debug operation.");
        else throw new XActionException( "Undefined debug operation: "+op);
      }
      catch( IOException e)
      {
        throw new XActionException( "Unable to execute debug operation.", e);
      }
    }
    
    return null;
  }
  
  private static ThreadLocal<Client> clients;
  
  private IExpression hostExpr;
  private IExpression portExpr;
  private IExpression opExpr;
  private IExpression timeoutExpr;
}
