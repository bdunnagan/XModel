package org.xmodel.xaction;

import org.xmodel.future.AsyncFuture;
import org.xmodel.log.SLog;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

/**
 * An XAction that waits for an AsyncFuture (returned by RunAction, for example).
 */
public class WaitAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    var = Conventions.getVarName( document.getRoot(), true);
    futureExpr = document.getExpression();
    timeoutExpr = document.getExpression( "timeout", true);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    try
    {
      int timeout = (timeoutExpr != null)? (int)timeoutExpr.evaluateNumber( context): Integer.MAX_VALUE;
      
      AsyncFuture<?> future = (AsyncFuture<?>)Conventions.getCache( context, futureExpr);
      if ( future != null) future.await( timeout); else SLog.warnf( this, "Future not found.");
      
      if ( future.isSuccess())
      {
        Object[] result = (Object[])future.getInitiator();
        if ( result != null) context.getScope().set( var, result[ 0]);
      }
      else
      {
        Throwable t = future.getFailureCause();
        throw new XActionException( t);
      }
      
      return null;
    }
    catch( InterruptedException e)
    {
      throw new XActionException( e);
    }
  }
  
  private String var;
  private IExpression futureExpr;
  private IExpression timeoutExpr;
}
