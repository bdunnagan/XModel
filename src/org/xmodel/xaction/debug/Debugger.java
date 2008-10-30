package org.xmodel.xaction.debug;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Semaphore;
import org.xmodel.IModelObject;
import org.xmodel.Xlate;
import org.xmodel.net.ModelServer;
import org.xmodel.xaction.IXAction;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.variable.IVariableScope;

/**
 * A class which manages the debugging execution context of an XAction script.
 */
public class Debugger implements IDebugger
{
  public Debugger( String threadID, String threadName, ModelServer server)
  {
    this.threadID = threadID;
    this.threadName = threadName;
    this.server = server;
    this.lock = new Semaphore( 0);
    this.stack = new Stack<Frame>();
    this.breakpoints = new ArrayList<Breakpoint>();
    this.step = Step.RESUME;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.debug.IDebugger#push(org.xmodel.xpath.expression.IContext, org.xmodel.xaction.IXAction)
   */
  public void push( IContext context, IXAction action)
  {
    // update stack
    Frame frame = new Frame();
    frame.context = context;
    frame.action = action;
    stack.push( frame);
    
    // suspend if necessary
    Step step = null;
    synchronized( this) { step = this.step;}
    
    if ( step == Step.SUSPEND) block( "request");
    if ( step == Step.STEP_INTO) block( "stepEnd");
    if ( steppedOver && step == Step.STEP_OVER) block( "stepEnd");
    
    // check breakpoints
    for( Breakpoint breakpoint: breakpoints)
      if ( isBreakpoint( breakpoint, action))
        block( "breakpoint");
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.debug.IDebugger#pop()
   */
  public Frame pop()
  {
    int stackSize = stack.size();
    if ( stackSize == 0) return null;
    
    Step step = null;
    synchronized( this) { step = this.step;}

    // compare current stack depth to saved stack depth and set flag to suspend next push
    if ( stepFrame == stackSize) steppedOver = true;
    
    // compare current stack depth to saved stack depth and complete step-return 
    if ( stepFrame == stackSize && step == Step.STEP_RETURN) block( "stepEnd");
    
    // update stack
    Frame frame = stack.pop();

    return frame;
  }
  
  /**
   * Block and send debug status to server.
   * @param action The action causing the block.
   */
  protected void block( String action)
  {
    server.sendDebugMessage( threadID, threadName, "suspended", action, stack);
    try { lock.acquire();} catch( InterruptedException e) {}
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.debug.IDebugger#report()
   */
  public void report()
  {
    server.sendDebugMessage( threadID, threadName, "report", null, null);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.debug.IDebugger#suspend()
   */
  public void suspend()
  {
    synchronized( this) { step = Step.SUSPEND;}
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.debug.IDebugger#resume()
   */
  public void resume()
  {
    synchronized( this) { step = Step.RESUME;}
    server.sendDebugMessage( threadID, threadName, "resumed", "request", null);
    lock.release();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.debug.IDebugger#stepInto()
   */
  public void stepInto()
  {
    if ( stack.size() == 0) return;
    
    synchronized( this) { step = Step.STEP_INTO;}
    server.sendDebugMessage( threadID, threadName, "resumed", "stepInto", null);
    lock.release();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.debug.IDebugger#stepOver()
   */
  public void stepOver()
  {
    if ( stack.size() == 0) return;
    
    synchronized( this) 
    { 
      step = Step.STEP_OVER;
      stepFrame = stack.size();
      steppedOver = false;
    }
    
    server.sendDebugMessage( threadID, threadName, "resumed", "stepOver", null);
    lock.release();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.debug.IDebugger#stepReturn()
   */
  public void stepReturn()
  {
    if ( stack.size() == 0) return;
    
    synchronized( this) 
    { 
      step = Step.STEP_RETURN;
      stepFrame = stack.size() - 1;
    }
    
    server.sendDebugMessage( threadID, threadName, "resumed", "stepReturn", null);
    lock.release();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.debug.IDebugger#createBreakpoint(java.lang.String, java.lang.String)
   */
  public void createBreakpoint( String path, String expression)
  {
    Breakpoint breakpoint = new Breakpoint();
    breakpoint.path = path;
    breakpoint.expression = XPath.createExpression( expression);
    breakpoints.add( breakpoint);    
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.debug.IDebugger#removeBreakpoint(java.lang.String, java.lang.String)
   */
  public void removeBreakpoint( String path, String expression)
  {
    Breakpoint breakpoint = new Breakpoint();
    breakpoint.path = path;
    breakpoint.expression = XPath.createExpression( expression);
    breakpoints.remove( breakpoint);    
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.debug.IDebugger#getVariableValue(int, java.lang.String)
   */
  public Object getVariableValue( int stackFrameIndex, String variable)
  {
    if ( stack.size() <= stackFrameIndex) return "";
    Frame frame = stack.get( stackFrameIndex);
    IVariableScope scope = frame.context.getScope();
    return scope.get( variable);
  }
  
  /**
   * Returns true if the specified action matches the breakpoint location.
   * @param breakpoint The breakpoint.
   * @param action The action.
   * @return Returns true if the specified action matches the breakpoint location.
   */
  protected boolean isBreakpoint( Breakpoint breakpoint, IXAction action)
  {
    IModelObject root = action.getDocument().getRoot();
    if ( root == null) return false;
    
    IModelObject ancestor = sourcePathExpr.queryFirst( root);
    if ( ancestor == null) return false;
    
    String path = Xlate.get( ancestor, "path", "");
    if ( path.equals( breakpoint.path))
    {
      IModelObject locus = breakpoint.expression.queryFirst( ancestor);
      return locus == root;
    }
    
    return false;
  }
  
  private class Breakpoint
  {
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals( Object object)
    {
      Breakpoint other = (Breakpoint)object;
      return other.path.equals( path) && other.expression.toString().equals( expression.toString());
    }
    
    String path;
    IExpression expression;
  }

  private final IExpression sourcePathExpr = XPath.createExpression(
    "ancestor-or-self::*[ @path]");
  
  private enum Step { RESUME, SUSPEND, STEP_INTO, STEP_OVER, STEP_RETURN};
  
  private String threadID;
  private String threadName;
  private ModelServer server;
  private Semaphore lock;
  private Stack<Frame> stack;
  private List<Breakpoint> breakpoints;
  private Step step;
  private boolean steppedOver;
  private int stepFrame;
}
