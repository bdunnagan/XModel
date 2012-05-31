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
    String op = opExpr.evaluateString( context);
    switch( Debugger.Operation.valueOf( op))
    {
      case stepOver: debugger.stepOver(); break;
      case stepIn:   debugger.stepIn(); break;
      case stepOut:  debugger.stepOut(); break;
      case resume:   debugger.resume(); break;
      case pause:    debugger.pause(); break;
      case fetch:    break;
    }

    context.getScope().set( var, debugger.getStack());    
    
    return null;
  }
  
  private IExpression opExpr;
  private String var;
}
