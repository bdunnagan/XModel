package org.xmodel.xaction;

import org.xmodel.IModelObject;
import org.xmodel.Xlate;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

/**
 * Increment the numeric value of a node or variable.
 */
public class DecrementAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    valueExpr = document.getExpression();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    if ( valueExpr != null)
    {
      for( IModelObject node: valueExpr.evaluateNodes( context))
      {
        Xlate.set( node, Xlate.get( node, 0d) - 1);
      }
    }
    
    return null;
  }

  private IExpression valueExpr;
}
