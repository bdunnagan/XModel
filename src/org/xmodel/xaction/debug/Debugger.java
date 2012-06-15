package org.xmodel.xaction.debug;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.xmodel.IModelObject;
import org.xmodel.ModelAlgorithms;
import org.xmodel.ModelObject;
import org.xmodel.Xlate;
import org.xmodel.log.SLog;
import org.xmodel.xaction.IXAction;
import org.xmodel.xaction.ScriptAction;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.variable.IVariableScope;

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
    threads = new ThreadLocal<DebugThread>();
    lock = new Semaphore( 0);
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
    DebugThread thread = getDebugThread();
    DebugFrame frame = new DebugFrame( context, action);
    thread.frames.add( frame);
    
    if ( thread.op == Operation.stepIn)
      thread.op = Operation.pause;
  }

  public Object[] run( IContext context, IXAction action)
  {
    DebugThread thread = getDebugThread();
    DebugFrame frame = new DebugFrame( context, action);
    Operation op;
    synchronized( this)
    {
      thread.frames.add( frame);
      op = thread.op;
    }

    if ( op == Operation.pause) block();
    
    try
    {
      Object[] result = action.run( context);
      return result;
    }
    finally
    {
      synchronized( this)
      {
        thread.frames.remove( frame);
      }
    }
  }

  public synchronized void pop()
  {
    DebugThread thread = getDebugThread();
    thread.frames.remove( thread.frames.size() - 1);
    
    if ( thread.frames.size() == 0)
    {
      threads.set( null);
    }
  }
  
  private void block()
  {
    try 
    { 
      lock.drainPermits();
      lock.acquire();
    } 
    catch( InterruptedException e) 
    {
      Thread.interrupted();
      SLog.warnf( this, "Debugger breakpoint interrupted.");
    }
  }

  private void unblock()
  {
    lock.release();
  }
    
  public synchronized IModelObject getStack()
  {
    IModelObject stackNode = new ModelObject( "stack");
    
    for( DebugThread thread: threadList)
    {
      IModelObject threadNode = new ModelObject( "thread");
      
      threadNode.setID( String.format( "%X", thread.hashCode()));
      Xlate.set( threadNode, "name", thread.thread);
      
      for( DebugFrame frame: thread.frames)
      {
        IModelObject frameNode = new ModelObject( "frame");
        
        frameNode.setID( String.format( "%X", frame.hashCode()));
        Xlate.set( frameNode, "name", frame.getName());
        
        IVariableScope scope = frame.context.getScope();
        for( String var: scope.getAll())
        {
          IModelObject varNode = new ModelObject( "var", var);
          frameNode.addChild( varNode);
        }
        
        threadNode.addChild( frameNode);
      }
      
      stackNode.addChild( threadNode);
    }
    
    return stackNode;
  }
  
  private final synchronized DebugThread getDebugThread()
  {
    DebugThread thread = threads.get();
    if ( thread == null)
    {
      thread = new DebugThread();
      threads.set( thread);
    }
    return thread;
  }
  
  private static class DebugThread
  {
    public DebugThread()
    {
      thread = Thread.currentThread().getName();
    }
    
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
 
    public String getName()
    {
      IModelObject config = action.getDocument().getRoot();
      
      String name = Xlate.get( config, "name", (String)null);
      if ( name != null) return name;
      
      return ModelAlgorithms.createIdentityPath( config, true).toString();
    }
    
    public IContext context;
    public IXAction action;
  }
  
  private ThreadLocal<DebugThread> threads;
  private List<DebugThread> threadList;
  private Semaphore lock;
}