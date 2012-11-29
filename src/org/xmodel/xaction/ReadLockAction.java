package org.xmodel.xaction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.xmodel.IModel;
import org.xmodel.IModelObject;
import org.xmodel.Xlate;
import org.xmodel.log.SLog;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;

/**
 * An XAction that will acquire a lock on its execution context, if that context is an instance of ThreadPoolContext,
 * or throw an IllegalStateException.
 */
public class ReadLockAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.ui.swt.form.actions.XAction#configure(org.xmodel.ui.model.ViewModel)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    onExpr = Xlate.get( document.getRoot(), "on", (IExpression)null);
    script = document.createScript();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    if ( onExpr == null)
    {
      try
      {
        context.getModel().readLock( 15, TimeUnit.MINUTES);
      }
      catch( InterruptedException e)
      {
        throw new XActionException( "Lock aborted without execution.", e);
      }
  
      try
      {
        StatefulContext readContext = new StatefulContext( context);
        return script.run( readContext);
      }
      finally
      {
        context.getModel().readUnlock();
      }
    }
    else
    {
      List<IModel> models = new ArrayList<IModel>( 1);
      try
      {
        for( IModelObject element: onExpr.evaluateNodes( context))
        {
          IModel model = element.getModel();
          if ( !models.contains( model)) 
          {
            models.add( model);
            model.readLock();
          }
        }
        
        script.run( context);
      }
      catch( InterruptedException e)
      {
        SLog.warn( this, "Thread interrupted - read lock aborted.");
      }
      finally
      {
        for( IModel model: models)
          model.readUnlock();
      }
    }
    
    return null;
  }
  
  private IExpression onExpr;
  private ScriptAction script;
}
