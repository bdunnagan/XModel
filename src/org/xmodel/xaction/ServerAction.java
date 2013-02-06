package org.xmodel.xaction;

import org.xmodel.net.XioServer;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

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
    
    addressExpr = document.getExpression( "address", true);
    portExpr = document.getExpression( "port", true);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    String address = addressExpr.evaluateString( context);
    int port = (int)portExpr.evaluateNumber( context);

    XioServer server = new XioServer( context);
    server.start( address, port);
    
    return null;
  }
  
  private IExpression addressExpr;
  private IExpression portExpr;
}
