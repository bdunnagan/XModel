package org.xmodel.xaction.debug;

import java.util.concurrent.Semaphore;
import org.xmodel.xaction.IXAction;
import org.xmodel.xaction.ScriptAction;
import org.xmodel.xpath.expression.IContext;

/**
 * A reference IDebugger implementation that uses an XPathServer to provide
 * access to the debugging operations and stack frame information.
 */
public class Debugger
{
  public final static int defaultPort = 27700;
  
  public Debugger()
  {
    semaphore = new Semaphore( 0);
    stepFrame = 1;
  }

  /**
   * @return Returns the current debugging context.
   */
  public synchronized IContext getContext()
  {
    return context;
  }
  
  /**
   * @return Returns the next action that will execute.
   */
  public synchronized IXAction getAction()
  {
    return action;
  }
  
  public synchronized void stepOver()
  {
    stepFrame = currFrame;
    unblock();
  }

  public synchronized void stepIn()
  {
    stepFrame = currFrame + 1;
    unblock();
  }

  public synchronized void stepOut()
  {
    stepFrame = currFrame - 1;
    unblock();
  }

  public void resume()
  {
    stepFrame = -1;
    unblock();
  }

  public synchronized void push( IContext context, ScriptAction action)
  {
    currFrame++;
    this.context = context;
    this.action = action;
  }

  public synchronized Object[] run( IContext context, IXAction action)
  {
    if ( stepFrame >= currFrame) block();
    Object[] result = action.run( context);
    return result;
  }

  public synchronized void pop()
  {
    currFrame--;
  }
  
  private void block()
  {
  }

  private void unblock()
  {
    semaphore.release();
  }

  private int stepFrame;
  private int currFrame;
  private IContext context;
  private IXAction action;
  private Semaphore semaphore;
}