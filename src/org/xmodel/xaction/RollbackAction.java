package org.xmodel.xaction;

import org.xmodel.external.GroupTransaction;
import org.xmodel.xpath.expression.IContext;

/**
 * Rollback the GroupTransaction in the calling context.
 */
public class RollbackAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    var = Conventions.getVarName( document.getRoot(), true); 
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    GroupTransaction transaction = TransactionAction.getTransaction( context);
    context.set( var, (transaction != null)? transaction.commit(): false);
    return null;
  }
  
  private String var;
}
