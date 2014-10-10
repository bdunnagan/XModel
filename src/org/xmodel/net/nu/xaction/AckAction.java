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
      ITransport transport = ((ITransport)object);
      IModelObject envelope = transport.getProtocol().envelope().getEnvelope( request);
      transport.sendAck( envelope);
    }

    return null;
  }
  
  private IExpression requestExpr;
}
