package org.xmodel.net.nu.xaction;

import org.xmodel.IModelObject;
import org.xmodel.net.nu.ITransport;
import org.xmodel.xaction.Conventions;
import org.xmodel.xaction.GuardedAction;
import org.xmodel.xaction.XActionDocument;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

public class AckAction extends GuardedAction
{
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    requestExpr = document.getExpression( "request", true);
  }

  @Override
  protected Object[] doAction( IContext context)
  {
    IModelObject request = requestExpr.queryFirst( context);
    
    Object object = Conventions.getCache( context, "via");
    if ( object != null && object instanceof ITransport)
    {
      ((ITransport)object).ack( request);
    }

    return null;
  }
  
  private IExpression requestExpr;
}
