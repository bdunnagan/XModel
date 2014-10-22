package org.xmodel.net.nu.xaction;

import org.xmodel.IModelObject;
import org.xmodel.net.nu.ITransport;
import org.xmodel.xaction.GuardedAction;
import org.xmodel.xaction.XActionDocument;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

public class DisconnectAction extends GuardedAction
{
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    transportExpr = document.getExpression();
  }

  @Override
  protected Object[] doAction( IContext context)
  {
    for( IModelObject element: transportExpr.evaluateNodes( context))
    {
      Object value = element.getValue();
      if ( value != null && value instanceof ITransport)
      {
        ((ITransport)value).disconnect( false);
      }
    }
    
    return null;
  }
  
  private IExpression transportExpr;
}
