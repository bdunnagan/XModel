package org.xmodel.xaction;

import org.xmodel.xaction.debug.Debugger;
import org.xmodel.xpath.expression.IContext;

public class BreakpointAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    Debugger debugger = XAction.getDebugger();
    debugger.breakpoint();
    return null;
  }
}
