package org.xmodel.xaction;

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
    enableExpr = document.getExpression();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    boolean enable = enableExpr.evaluateBoolean( context);
    XAction.setDebugger( enable? new Debugger(): null);
    return null;
  }
  
  private IExpression enableExpr;
}
