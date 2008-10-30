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
   * Create a breakpoint on the specified file at the specified document locus.
   * @param path The fully-qualified path of the file.
   * @param expression The expression giving the document locus.
   */
  public void createBreakpoint( String path, String expression);
  
  /**
   * Clear the breakpoint on the specified file at the specified document locus.
   * @param path The fully-qualified path of the file.
   * @param expression The expression giving the document locus.
   */
  public void removeBreakpoint( String path, String expression);
  
  /**
   * Returns the value of the variable in the specified stack frame.
   * @param stackFrameIndex The index of the stack frame.
   * @param variable The name of the variable.
   * @return Returns the value of the variable.
   */
  public Object getVariableValue( int stackFrameIndex, String variable);
  
  public class Frame
  {
    public IContext context;
    public IXAction action;
  }
}