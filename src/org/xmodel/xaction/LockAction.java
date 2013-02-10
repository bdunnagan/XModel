package org.xmodel.xaction;

import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

/**
 * An XAction that will acquire a lock on its execution context, if that context is an instance of ThreadPoolContext,
 * or throw an IllegalStateException.
 */
public class LockAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.ui.swt.form.actions.XAction#configure(org.xmodel.ui.model.ViewModel)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    onExpr = document.getExpression( "on", true);
    script = document.createScript();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    List<IModelObject> nodes = onExpr.evaluateNodes( context);
    if ( nodes.size() != 1) 
    {
      throw new XActionException( "Target node-set of LockAction must contain exactly 1 node.");
    }
    
    synchronized( nodes.get( 0))
    {
      return script.run( context);
    }
  }
  
  private IExpression onExpr;
  private ScriptAction script;
}
