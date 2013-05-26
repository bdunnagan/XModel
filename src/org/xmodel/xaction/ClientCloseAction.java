package org.xmodel.xaction;

import org.xmodel.log.SLog;
import org.xmodel.net.XioClient;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

/**
 * Close a client that was previously connected by ClientAction.
 */
public class ClientCloseAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    clientExpr = document.getExpression();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    XioClient client = (XioClient)Conventions.getCache( context, clientExpr);
    if ( client != null) 
    {
      try { client.unregisterAll();} catch( Exception e) { SLog.exception( this, e);}
      client.setAutoReconnect( false);
      client.close();
    }
    return null;
  }

  private IExpression clientExpr;
}
