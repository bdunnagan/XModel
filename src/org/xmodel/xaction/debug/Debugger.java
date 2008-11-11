package org.xmodel.xaction.debug;

import java.util.List;
import java.util.Stack;
import java.util.Vector;
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
    this.breakpoints = new Vector<Breakpoint>();
    this.step = Step.RESUME;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.debug.IDebugger#push(org.xmodel.xpath.expression.IContext, org.xmodel.xaction.IXAction)
   */
  public void push( IContext context, IXAction action)
  {
    Frame parent = stack.isEmpty()? null: stack.peek();
    
    // update stack
    Frame frame = new Frame();
    frame.parent = parent;
    frame.context = context;
    frame.action = action;
    stack.push( frame);
    
    // suspend if necessary
    Step step = null;
    synchronized( this) { step = this.step;}
    
    if ( step == Step.SUSPEND) 
    {
      block( "request");
    }
    else if ( step == Step.STEP_INTO) 
    {
      block( "stepEnd");
    }
    else if ( step == Step.STEP_OVER && pending != null && parent == pending.parent) 
    {
      block( "stepEnd");
    }
    else if ( step == Step.STEP_RETURN && pending != null && pending.parent != null && parent == pending.parent.parent)
    {
      block( "stepEnd");
    }
    
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
    if ( stack.isEmpty()) return null;

    Step step = null;
    synchronized( this) { step = this.step;}
    
    // check for script ending
    if ( (step == Step.STEP_INTO || step == Step.STEP_OVER || step == Step.STEP_RETURN) && scriptEnding)
    {
      scriptEnding = false;
      block( "scriptEnd");
    }
    
    return stack.pop();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.debug.IDebugger#scriptEnding()
   */
  public void scriptEnding()
  {
    if ( pending != null) scriptEnding = true;
  }

  /**
   * Block and send debug status to server.
   * @param action The action causing the block.
   */
  protected void block( String action)
  {
    pending = stack.peek();
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
    if ( pending != null)
    {
      synchronized( this) { step = Step.RESUME;}
    
      // ignore exceptions during resume in case the socket has already been closed
      try { server.sendDebugMessage( threadID, threadName, "resumed", "request", null);} catch( Exception e) {}
    
      lock.release();
      pending = null;
    }
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.debug.IDebugger#stepInto()
   */
  public void stepInto()
  {
    if ( stack.isEmpty()) return;
    synchronized( this) { step = Step.STEP_INTO;}
    server.sendDebugMessage( threadID, threadName, "resumed", "stepInto", null);
    lock.release();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.debug.IDebugger#stepOver()
   */
  public void stepOver()
  {
    if ( stack.isEmpty()) return;
    synchronized( this) { step = Step.STEP_OVER;}
    server.sendDebugMessage( threadID, threadName, "resumed", "stepOver", null);
    lock.release();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.debug.IDebugger#stepReturn()
   */
  public void stepReturn()
  {
    if ( stack.isEmpty()) return;
    synchronized( this) { step = Step.STEP_RETURN;}
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
    
    String path = Xlate.get( ancestor, "url", "");
    if ( path.endsWith( breakpoint.path))
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
    "ancestor-or-self::*[ @url]");
  
  private enum Step { RESUME, SUSPEND, STEP_INTO, STEP_OVER, STEP_RETURN};
  
  private String threadID;
  private String threadName;
  private ModelServer server;
  private Semaphore lock;
  private Stack<Frame> stack;
  private List<Breakpoint> breakpoints;
  private Step step;
  private Frame pending;
  private boolean scriptEnding;
}
