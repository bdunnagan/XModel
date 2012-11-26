package org.xmodel.xaction;

import org.xmodel.IDispatcher;
import org.xmodel.IModelObject;
import org.xmodel.Model;
import org.xmodel.ModelObject;
import org.xmodel.concurrent.ParallelExecutorDispatcher;
import org.xmodel.concurrent.SerialExecutorDispatcher;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

/**
 * Create a thread-pool and dispatcher, and assign it to a variable for later asynchronous execution via RunAction.
 */
public class DispatcherAction extends XAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.XAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    var = Conventions.getVarName( document.getRoot(), true);
    typeExpr = document.getExpression( "type", true);
    threadsExpr = document.getExpression( "threads", true);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.XAction#doRun(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public Object[] doRun( IContext context)
  {
    int threads = (threadsExpr != null)? (int)threadsExpr.evaluateNumber( context): 0;
    String type = (typeExpr != null)? typeExpr.evaluateString( context): "serial";
    IDispatcher dispatcher = createDispatcher( type, threads);
    
    IModelObject holder = new ModelObject( "dispatcher");
    holder.setValue( dispatcher);
    context.set( var, holder);
    
    return null;
  }
  
  /**
   * Create the dispatcher for the thread pool.
   * @param type The type of dispatcher.
   * @param threads The number of threads.
   * @return Returns the new IDispatcher instance.
   */
  private IDispatcher createDispatcher( String type, int threads)
  {
    if ( type.equals( "serial"))
    {
      return new SerialExecutorDispatcher( new Model(), threads);
    }
    else if ( type.equals( "parallel"))
    {
      return new ParallelExecutorDispatcher( threads);
    }
    else
    {
      throw new XActionException( "Type must be one of 'serial' or 'parallel'.");
    }
  }
  
  private String var;
  private IExpression typeExpr;
  private IExpression threadsExpr;
}
