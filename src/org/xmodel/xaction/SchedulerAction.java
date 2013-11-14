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
    var = Conventions.getVarName( document.getRoot(), false);
    nameExpr = document.getExpression( "name", true);
    threadsExpr = document.getExpression( "threads", true);
    targetExpr = document.getExpression( "target", true);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.XAction#doRun(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public Object[] doRun( IContext context)
  {
    int threads = (threadsExpr != null)? (int)threadsExpr.evaluateNumber( context): 1;
    
    IModelObject target = (targetExpr != null)? targetExpr.queryFirst( context): null;
    String name = (nameExpr != null)? nameExpr.evaluateString( context): ((target != null)? target.getType(): "model-timer");
    
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool( threads, new ModelThreadFactory( name));
    
    if ( target == null) target = new ModelObject( "scheduler");
    target.setValue( scheduler);
    if ( var != null) context.set( var, target);
    
    return null;
  }
  
  private String var;
  private IExpression nameExpr;
  private IExpression threadsExpr;
  private IExpression targetExpr;
}
