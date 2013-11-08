package org.xmodel.xaction;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.xmodel.IModelObject;
import org.xmodel.ModelObject;
import org.xmodel.concurrent.ModelThreadFactory;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

/**
 * Create a ScheduledExecutorService.
 */
public class SchedulerAction extends XAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.XAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    var = Conventions.getVarName( document.getRoot(), true);
    nameExpr = document.getExpression( "name", true);
    threadsExpr = document.getExpression( "threads", true);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.XAction#doRun(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public Object[] doRun( IContext context)
  {
    int threads = (threadsExpr != null)? (int)threadsExpr.evaluateNumber( context): 1;
    String name = (nameExpr != null)? nameExpr.evaluateString( context): "model-timer";
    
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool( threads, new ModelThreadFactory( name));
    
    IModelObject holder = new ModelObject( "executor");
    holder.setValue( scheduler);
    context.set( var, holder);
    
    return null;
  }
  
  private String var;
  private IExpression nameExpr;
  private IExpression threadsExpr;
}
