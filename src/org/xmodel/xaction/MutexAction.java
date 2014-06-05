package org.xmodel.xaction;

import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

/**
 * An XAction that will acquire a mutex on either the context, or the IModelObject returned
 * by the <i>on</i> expression. 
 */
public class MutexAction extends GuardedAction
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
    Object lock = context;
    
    List<IModelObject> nodes = (onExpr != null)? onExpr.evaluateNodes( context): null;
    if ( nodes != null && nodes.size() > 0) lock = nodes.get( 0);

    if ( lock != null)
    {
      synchronized( lock)
      {
        return script.run( context);
      }
    }
    else
    {
      return script.run( context);
    }
  }
  
  private IExpression onExpr;
  private ScriptAction script;
}
