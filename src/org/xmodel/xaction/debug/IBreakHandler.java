package org.xmodel.xaction.debug;

import org.xmodel.xpath.expression.IContext;

/**
 * An interface that defines how a breakpoint is implemented by the debugger.
 */
public interface IBreakHandler
{
  /**
   * Called when a breakpoint is hit.
   * @param context The execution context.
   */
  public void breakpoint( IContext context);
  
  /**
   * Called when the debugger is instructed to resume execution.
   */
  public void resume();
}
