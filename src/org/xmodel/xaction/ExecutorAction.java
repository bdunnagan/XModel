package org.xmodel.xaction;

import java.util.concurrent.Executor;
import org.xmodel.IModelObject;
import org.xmodel.ModelObject;
import org.xmodel.concurrent.LoggingExecutorWrapper;
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
    int threads = (threadsExpr != null)? (int)threadsExpr.evaluateNumber( context): 0;
    String name = (nameExpr != null)? nameExpr.evaluateString( context): "model";
    Executor executor = new LoggingExecutorWrapper( name, threads);
    
    IModelObject holder = new ModelObject( "executor");
    holder.setValue( executor);
    context.set( var, holder);
    
    return null;
  }
  
  private String var;
  private IExpression nameExpr;
  private IExpression threadsExpr;
}
