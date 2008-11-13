package org.xmodel.xaction.debug;

import org.xmodel.xaction.IXAction;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

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
   * Create a breakpoint on the specified action path.
   * @param file The name of the file.
   * @param path The identity path of the action.
   */
  public void createBreakpoint( String file, String path);
  
  /**
   * Clear the breakpoint on the specified action path.
   * @param file The name of the file.
   * @param path The identity path of the action.
   */
  public void removeBreakpoint( String file, String path);
  
  /**
   * Returns the value of the variable in the specified stack frame.
   * @param stackFrameIndex The index of the stack frame.
   * @param variable The name of the variable.
   * @return Returns the value of the variable.
   */
  public Object getVariableValue( int stackFrameIndex, String variable);
  
  /**
   * Set the file and script filters defined by the client. The file filter expression
   * returns the url of the file containing the action. The script filter expression
   * returns the root of the script containing the action.  Both expression are
   * evaluated with the action element.
   * @param fileFilter The file filter.
   * @param scriptFilter The script filter.
   */
  public void setFilters( IExpression fileFilter, IExpression scriptFilter);
  
  public class Frame
  {
    public Frame parent;
    public IContext context;
    public IXAction action;
  }
}