package org.xmodel.xaction;

import java.util.concurrent.Executor;
import org.xmodel.IModelObject;
import org.xmodel.ModelObject;
import org.xmodel.concurrent.ThreadPoolExecutor;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

/**
 * Create an Executor instance.
 */
public class ExecutorAction extends XAction
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
    
    maxThreadsExpr = document.getExpression( "maxThreads", true);
    minThreadsExpr = document.getExpression( "minThreads", true);
    if ( minThreadsExpr == null) minThreadsExpr = document.getExpression( "threads", true);
    
    lingerExpr = document.getExpression( "linger", true);
    targetExpr = document.getExpression( "target", true);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.XAction#doRun(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public Object[] doRun( IContext context)
  {
    int minThreads = (minThreadsExpr != null)? (int)minThreadsExpr.evaluateNumber( context): 0;
    int maxThreads = (maxThreadsExpr != null)? (int)maxThreadsExpr.evaluateNumber( context): 0;
    int linger = (lingerExpr != null)? (int)lingerExpr.evaluateNumber( context): Integer.MAX_VALUE;
    if ( linger < 0) linger = Integer.MAX_VALUE;
    
    IModelObject target = (targetExpr != null)? targetExpr.queryFirst( context): null;
    String name = (nameExpr != null)? nameExpr.evaluateString( context): ((target != null)? target.getType(): "model");
    
    Executor executor = new ThreadPoolExecutor( name, minThreads, maxThreads, linger);
    
    if ( target == null) target = new ModelObject( "executor");
    target.setValue( executor);
    if ( var != null) context.set( var, target);
    
    return null;
  }
  
  private String var;
  private IExpression nameExpr;
  private IExpression minThreadsExpr;
  private IExpression maxThreadsExpr;
  private IExpression targetExpr;
  private IExpression lingerExpr;
}
