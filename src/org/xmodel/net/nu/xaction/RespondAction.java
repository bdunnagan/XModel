package org.xmodel.net.nu.xaction;

import org.xmodel.IModelObject;
import org.xmodel.log.Log;
import org.xmodel.net.nu.ITransport;
import org.xmodel.xaction.GuardedAction;
import org.xmodel.xaction.XActionDocument;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

public class RespondAction extends GuardedAction
{
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);

    viaExpr = document.getExpression( "via", true);
    
    requestExpr = document.getExpression( "request", true);

    if ( document.getRoot().getNumberOfChildren() == 0)
      messageExpr = document.getExpression();
  }

  @Override
  protected Object[] doAction( IContext context)
  {
    IModelObject viaNode = viaExpr.queryFirst( context);
    IModelObject request = requestExpr.queryFirst( context);
    IModelObject message = (messageExpr != null)? messageExpr.queryFirst( context): ActionUtil.getMessage( document.getRoot());
    
    Object object = viaNode.getValue();
    if ( object != null && object instanceof ITransport)
    {
      ((ITransport)object).respond( message, request);
    }

    return null;
  }
  
  public final static Log log = Log.getLog( RespondAction.class);
  
  private IExpression viaExpr;
  private IExpression messageExpr;
  private IExpression requestExpr;
}
