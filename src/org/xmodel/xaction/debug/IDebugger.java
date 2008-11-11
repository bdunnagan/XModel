package org.xmodel.xaction.debug;

import org.xmodel.xaction.IXAction;
import org.xmodel.xpath.expression.IContext;

public interface IDebugger
{
  /**
   * Push the specified action on the stack.
   * @param context The context.
   * @param action The action.
   */
  public void push( IContext context, IXAction action);

  /**
   * Pop the current action off the stack.
   * @return Returns the frame that was popped.
   */
  public Frame pop();

  /**
   * Called by a script to force the debugger to insert an extra step after
   * the next call to <code>pop</code>.  This method gives the client an 
   * opportunity to examine state after the last action in a script runs.
   */
  public void scriptEnding();
  
  /**
   * Send a debug report message (used to obtain the list of running threads).
   */
  public void report();
  
  /**
   * Suspend xaction processing beginning with the next xaction to be executed.
   */
  public void suspend();
  
  /**
   * Resume xaction processing.
   */
  public void resume();
  
  /**
   * Suspend execution of the next xaction.
   */
  public void stepInto();

  /**
   * Suspend execution of the next xaction in the currently executing script.
   */
  public void stepOver();
  
  /**
   * Unwind the stack by one frame.
   */
  public void stepReturn();
  
  /**
   * Create a breakpoint on the specified action path.
   * @param path The identity path of the action.
   */
  public void createBreakpoint( String path);
  
  /**
   * Clear the breakpoint on the specified action path.
   * @param path The identity path of the action.
   */
  public void removeBreakpoint( String path);
  
  /**
   * Returns the value of the variable in the specified stack frame.
   * @param stackFrameIndex The index of the stack frame.
   * @param variable The name of the variable.
   * @return Returns the value of the variable.
   */
  public Object getVariableValue( int stackFrameIndex, String variable);
  
  public class Frame
  {
    public Frame parent;
    public IContext context;
    public IXAction action;
  }
}