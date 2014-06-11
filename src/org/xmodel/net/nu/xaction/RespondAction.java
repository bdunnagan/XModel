package org.xmodel.net.nu.xaction;

import org.xmodel.IModelObject;
import org.xmodel.log.Log;
import org.xmodel.net.nu.ITransport;
import org.xmodel.xaction.Conventions;
import org.xmodel.xaction.GuardedAction;
import org.xmodel.xaction.XActionDocument;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

public class RespondAction extends GuardedAction
{
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);

    requestExpr = document.getExpression( "request", true);
    if ( requestExpr == null) requestExpr = XPath.createExpression( "$message");

    if ( document.getRoot().getNumberOfChildren() == 0)
      messageExpr = document.getExpression();
  }

  @Override
  protected Object[] doAction( IContext context)
  {
    IModelObject request = requestExpr.queryFirst( context);
    IModelObject message = (messageExpr != null)? messageExpr.queryFirst( context): MessageSchema.getMessage( document.getRoot());
    
    Object object = Conventions.getCache( context, "via");
    if ( object != null && object instanceof ITransport)
    {
      ((ITransport)object).respond( message, request);
    }

    return null;
  }
  
  public final static Log log = Log.getLog( RespondAction.class);
  
  private IExpression messageExpr;
  private IExpression requestExpr;
}