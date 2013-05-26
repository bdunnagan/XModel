package org.xmodel.xaction;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.xmodel.IModelObject;
import org.xmodel.Xlate;
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
    
    var = Conventions.getVarName( document.getRoot(), false);
    onExpr = Xlate.get( document.getRoot(), "on", (IExpression)null);
    if ( onExpr == null) onExpr = Xlate.get( document.getRoot(), "set", (IExpression)null);
    timeoutExpr = document.getExpression( "timeout", true);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    List<IModelObject> set = onExpr.evaluateNodes( context);
    if ( set.size() == 0) return null;
    
    GroupTransaction group = getTransaction( context);
    if ( group == null)
    {
      group = new GroupTransaction();
      setTransaction( context, group);
    }
    
    for( IModelObject node: set)
    {
      if ( !(node instanceof IExternalReference)) continue;
      
      IExternalReference reference = (IExternalReference)node;
      ITransaction transaction = reference.getCachingPolicy().transaction();
      transaction.track( reference);
      group.addTransaction( transaction);
    }

    // lock
    int timeout = (timeoutExpr != null)? (int)timeoutExpr.evaluateNumber( context): Integer.MAX_VALUE;
    group.lock(  timeout);
    
    // execute script
    try
    {
      super.doAction( context);

      // commit, if necessary
      if ( group.state() == State.locked)
      {
        boolean result = group.commit();
        if ( var != null)
        {
          context.getScope().set( var, result);
        }
        else if ( !result)
        {
          throw new XActionException( String.format(
            "Transaction commit failed for %s", onExpr
            ));
        }
      }
    }
    finally
    {
      // unlock
      group.unlock();
      
      // clear transaction
      setTransaction( context, null);
    }

    return null;
  }
   
  /**
   * Returns the GroupTransaction instance in the specified context.
   * @param context The context.
   * @return Returns null or the GroupTransaction.
   */
  public static GroupTransaction getTransaction( IContext context)
  {
    return transactions.get( context);
  }
  
  /**
   * Set the transaction in the specified context.
   * @param context The context.
   * @param transaction The transaction.
   */
  private void setTransaction( IContext context, GroupTransaction transaction)
  {
    if ( transaction != null)
    {
      transactions.put( context, transaction);
    }
    else
    {
      transactions.remove(  context);
    }
  }
  
  private String var;
  private IExpression onExpr;
  private IExpression timeoutExpr;
  private static Map<IContext, GroupTransaction> transactions = new ConcurrentHashMap<IContext, GroupTransaction>();
}
