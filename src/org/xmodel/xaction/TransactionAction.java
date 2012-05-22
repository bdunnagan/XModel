package org.xmodel.xaction;

import org.xmodel.IModelObject;
import org.xmodel.external.GroupTransaction;
import org.xmodel.external.IExternalReference;
import org.xmodel.external.ITransaction;
import org.xmodel.external.ITransaction.State;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

public class TransactionAction extends ScriptAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.ScriptAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    var = Conventions.getVarName( document.getRoot(), true);
    setExpr = document.getExpression( "set", true);
    timeoutExpr = document.getExpression( "timeout", true);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    GroupTransaction group = new GroupTransaction();
    for( IModelObject node: setExpr.evaluateNodes( context))
    {
      if ( !(node instanceof IExternalReference)) continue;
      
      IExternalReference reference = (IExternalReference)node;
      ITransaction transaction = reference.getCachingPolicy().transaction();
      group.addTransaction( transaction);
    }

    // store transaction in context
    context.getScope().set( var, group); 
    
    // lock
    int timeout = (timeoutExpr != null)? (int)timeoutExpr.evaluateNumber( context): Integer.MAX_VALUE;
    group.lock(  timeout);
    
    // execute script
    try
    {
      super.doAction( context);

      // commit, if necessary
      if ( group.state() == State.lock)
      {
        boolean result = group.commit();
        context.getScope().set( var, result);
      }
    }
    finally
    {
      // unlock
      group.unlock();
    }

    return null;
  }

  private String var;
  private IExpression setExpr;
  private IExpression timeoutExpr;
}
