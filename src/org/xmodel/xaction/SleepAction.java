package org.xmodel.xaction;

import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

public class SleepAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    sleepExpr = document.getExpression();
  }

  @Override
  protected Object[] doAction( IContext context)
  {
    int time = (int)sleepExpr.evaluateNumber( context);
    if ( time > 0)
    {
      try 
      {
        Thread.sleep( time);
      }
      catch( InterruptedException e)
      {
        Thread.interrupted();
      }
    }
    return null;
  }

  private IExpression sleepExpr;
}
