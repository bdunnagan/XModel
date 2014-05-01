package org.xmodel.xaction;

import org.xmodel.GlobalSettings;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

public class DeleteContextAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    nameExpr = document.getExpression( "name", true);
  }

  @Override
  protected Object[] doAction( IContext context)
  {
    GlobalSettings.getInstance().deleteContext( nameExpr.evaluateString( context));
    return null;
  }

  private IExpression nameExpr;
}
