package org.xmodel.xaction;

import org.xmodel.xaction.debug.Debugger;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

/**
 * An XAction that performs debugging operations.
 */
public class DebuggerAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    var = Conventions.getVarName( document.getRoot(), true);
    threadExpr = document.getExpression( "thread", true);
    opExpr = document.getExpression( "op", true);
    if ( opExpr == null) opExpr = document.getExpression();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    Debugger debugger = XAction.getDebugger();
    String thread = (threadExpr != null)? threadExpr.evaluateString( context): null;
    String op = opExpr.evaluateString( context);
    switch( Debugger.Operation.valueOf( op))
    {
      case stepOver: debugger.stepOver( thread); break;
      case stepIn:   debugger.stepIn( thread); break;
      case stepOut:  debugger.stepOut( thread); break;
      case resume:   debugger.resume( thread); break;
      case pause:    debugger.pause( thread); break;
      case sync:    break;
    }

    context.getScope().set( var, debugger.getStack());    
    
    return null;
  }
  
  private String var;
  private IExpression threadExpr;
  private IExpression opExpr;
}
