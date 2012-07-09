package org.xmodel.xaction;

import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

/**
 * An XAction that evaluates an expression solely for the purpose of synchronizing external references.
 */
public class TouchAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    touchExpr = document.getExpression();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    switch( touchExpr.getType( context))
    {
      case NODES:   touchExpr.evaluateNodes( context); break;
      case STRING:  touchExpr.evaluateString( context); break;
      case NUMBER:  touchExpr.evaluateNumber( context); break;
      case BOOLEAN: touchExpr.evaluateBoolean( context); break;
    }
    
    return null;
  }
  
  private IExpression touchExpr;
}
