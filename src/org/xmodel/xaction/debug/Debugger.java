package org.xmodel.xaction.debug;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import org.xmodel.IModelObject;
import org.xmodel.IPath;
import org.xmodel.ModelAlgorithms;
import org.xmodel.ModelObject;
import org.xmodel.Xlate;
import org.xmodel.log.SLog;
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
  
  public static enum Operation { fetch, stepOver, stepIn, stepOut, pause, resume};
  
  public Debugger()
  {
    stack = new DebugStack();
    threads = new ThreadLocal<DebugThread>();
  }

  public synchronized IModelObject getStack()
  {
  }
  
  public synchronized void stepOver()
  {
    DebugThread thread = getDebugThread();
    thread.op = Operation.stepOver;
    unblock();
  }

  public synchronized void stepIn()
  {
    DebugThread thread = getDebugThread();
    thread.op = Operation.stepIn;
    unblock();
  }

  public synchronized void stepOut()
  {
    DebugThread thread = getDebugThread();
    thread.op = Operation.stepOut;
    unblock();
  }

  public synchronized void pause()
  {
    DebugThread thread = getDebugThread();
    thread.op = Operation.pause;
  }
  
  public synchronized void breakpoint()
  {
    DebugThread thread = getDebugThread();
    thread.op = Operation.pause;
    block();
  }
  
  public synchronized void resume()
  {
    DebugThread thread = getDebugThread();
    if ( thread.op != Operation.resume)
    {
      thread.op = Operation.resume;
      unblock();
    }
  }

  public synchronized void push( IContext context, ScriptAction action)
  {
  }

  public Object[] run( IContext context, IXAction action)
  {
    boolean block = false;
    synchronized( this) 
    { 
      block = stepFrame >= currFrame;
      buildFrame( context, action);
    }
    
    if ( block) block();
    
    Object[] result = action.run( context);
    return result;
  }

  public synchronized void pop()
  {
    currFrame--;
    
    // remove current frame
    if ( frame != null && frame != stack) 
    {
      IModelObject parent = frame.getParent();
      frame.removeFromParent();
      frame = parent;
    }
  }
  
  private void block()
  {
    try 
    { 
      SLog.infof( this, "Debugger blocking ...");
      semaphore.acquire();
      
      SLog.infof( this, "Debugger resuming.");
    } 
    catch( InterruptedException e) 
    {
      SLog.info( this, "Debugger resuming after interruption.");
    }
  }

  private void unblock()
  {
    semaphore.release();
  }
  
  private void buildFrame( IContext context, IXAction action)
  {
    // clean old frame
    frame.removeChildren( "var");
    
    // location
    if ( frame.getParent().isType( "stack"))
    {
      IPath path = ModelAlgorithms.createIdentityPath( action.getDocument().getRoot(), true);
      Xlate.set( frame, "location", path.toString());
    }
    else
    {
      Xlate.set( frame, "location", action.toString());
    }
    
    // store variable names in scope
    for( String varName: context.getScope().getVariables())
    {
      ModelObject varNode = new ModelObject( "var");
      varNode.setValue( varName);
      frame.addChild( varNode);
    }
  }
  
  private final synchronized DebugThread getDebugThread()
  {
    DebugThread thread = threads.get();
    if ( thread == null)
    {
      thread = new DebugThread();
      threads.set( thread);
      stack.threads.add( thread);
    }
    return thread;
  }
  
  private static class DebugStack
  {
    public List<DebugThread> threads = new ArrayList<DebugThread>();
  }
  
  private static class DebugThread
  {
    public DebugThread()
    {
      thread = Thread.currentThread().getName();
    }
    
    public DebugStack parent;
    public List<DebugFrame> frames = new ArrayList<DebugFrame>();
    public final String thread;
    public Operation op;
  }
  
  private static class DebugFrame
  {
    public DebugFrame( IContext context, IXAction action)
    {
      this.context = context;
      this.action = action;
    }
 
    public DebugThread parent;
    public IContext context;
    public IXAction action;
  }
  
  private DebugStack stack;
  private ThreadLocal<DebugThread> threads;
}