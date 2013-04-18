package org.xmodel.xaction.debug;

import java.util.ArrayList;
import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.ModelObject;
import org.xmodel.Xlate;
import org.xmodel.xaction.IXAction;
import org.xmodel.xaction.ScriptAction;
import org.xmodel.xml.IXmlIO.Style;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.variable.IVariableScope;

/**
 * A reference IDebugger implementation that uses an XPathServer to provide
 * access to the debugging operations and stack frame information.
 */
public class Debugger
{
  public static enum Operation { sync, stepOver, stepIn, stepOut, pause, resume};
  
  public Debugger()
  {
    threads = new ThreadLocal<DebugThread>();
    threadList = new ArrayList<DebugThread>();
  }    
  
  public synchronized void setBreakHandler( IBreakHandler breaker)
  {
    this.breaker = breaker;
  }
  
  public synchronized IBreakHandler getBreakHandler()
  {
    return breaker;
  }

  public synchronized void stepOver( String threadID)
  {
    DebugThread thread = getDebugThread( threadID);
    thread.op = Operation.stepOver;
    breaker.resume();
  }

  public synchronized void stepIn( String threadID)
  {
    DebugThread thread = getDebugThread( threadID);
    thread.op = Operation.stepIn;
    breaker.resume();
  }

  public synchronized void stepOut( String threadID)
  {
    DebugThread thread = getDebugThread( threadID);
    thread.op = Operation.stepOut;
    breaker.resume();
  }

  public synchronized void pause( String threadID)
  {
    DebugThread thread = getDebugThread( threadID);
    thread.op = Operation.pause;
  }
  
  public void breakpoint( IContext context)
  {
    synchronized( this)
    {
      DebugThread thread = getDebugThread();
      thread.op = Operation.pause;
    }
    
    breaker.breakpoint( context);
  }
  
  public synchronized void resume( String threadID)
  {
    DebugThread thread = getDebugThread( threadID);
    if ( thread.op != Operation.resume)
    {
      thread.op = Operation.resume;
      breaker.resume();
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

    if ( op == Operation.pause || op == Operation.stepOver) breaker.breakpoint( context);
    
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

  public void pop( IContext context)
  {
    boolean shouldBreak = false;
    
    synchronized( this)
    {
      DebugThread thread = getDebugThread();
      thread.frames.remove( thread.frames.size() - 1);
      
      if ( thread.frames.size() == 0)
      {
        threads.set( null);
        threadList.remove( thread);
      }
      
      if ( thread.op == Operation.pause || thread.op == Operation.stepOver) 
        shouldBreak = true;
      
      if ( thread.op == Operation.stepOut)
        thread.op = Operation.pause;
    }
    
    if ( shouldBreak) breaker.breakpoint( context);
  }
  
  public synchronized IModelObject getStack()
  {
    IModelObject stackNode = new ModelObject( "stack");
    
    for( DebugThread thread: threadList)
    {
      IModelObject threadNode = new ModelObject( "thread");
      threadNode.setID( String.format( "%X", thread.hashCode()));
      Xlate.set( threadNode, "name", thread.thread);
      
      IContext frameContext = null;
      for( int i=0; i<thread.frames.size(); i++)
      {
        DebugFrame frame = thread.frames.get( i);
        
        IModelObject frameNode = new ModelObject( "frame");
        frameNode.setID( String.format( "%X", frame.hashCode()));
        Xlate.set( frameNode, "name", frame.getName());
        
        if ( frameContext != frame.context)
        {
          IVariableScope scope = frame.context.getScope();
          for( String var: (i==0)? scope.getAll(): scope.getVariables())
          {
            IModelObject varNode = new ModelObject( "var", var);
            Object object = scope.get( var);
            serializeVariable( varNode, object);
            frameNode.addChild( varNode);
          }
          frameContext = frame.context;
        }
        
        threadNode.addChild( frameNode);
      }
      stackNode.addChild( threadNode);
    }
    
    return stackNode;
  }
  
  public synchronized IXAction getCurrentFrame( String threadID)
  {
    DebugThread thread = getDebugThread( threadID);
    if ( thread.frames != null && thread.frames.size() > 0)
    {
      return thread.frames.get( thread.frames.size() - 1).action;
    }
    return null;
  }
  
  /**
   * Serialize the specified variable value into the specified stack variable node.
   * @param varNode The stack variable node.
   * @param value The value of the variable.
   */
  private static void serializeVariable( IModelObject varNode, Object value)
  {
    if ( value instanceof List)
    {
      Xlate.set( varNode, "type", "nodes");
      for( Object object: (List<?>)value)
      {
        varNode.addChild( ((IModelObject)object).cloneTree());
      }
    }
    else
    {
      Xlate.set( varNode, "type", "value");
      varNode.setValue( value);
    }
  }
  
  public final synchronized String getDebugThreadID()
  {
    DebugThread thread = getDebugThread();
    return String.format( "%X", thread.hashCode());
  }
  
  private final synchronized DebugThread getDebugThread()
  {
    DebugThread thread = threads.get();
    if ( thread == null)
    {
      thread = new DebugThread();
      threads.set( thread);
      threadList.add( thread);
    }
    return thread;
  }
  
  private final synchronized DebugThread getDebugThread( String threadID)
  {
    for( DebugThread thread: threadList)
    {
      String id = String.format( "%X", thread.hashCode());
      if ( id.equals( threadID)) return thread;
    }
    return null;
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
      IModelObject config = action.getDocument().getRoot().cloneObject();
      config.removeAttribute( "xaction");
      config.removeAttribute( "xm:compiled");
      return XmlIO.write( Style.printable, config);
    }
        
    public IContext context;
    public IXAction action;
  }
 
  private IBreakHandler breaker;
  private ThreadLocal<DebugThread> threads;
  private List<DebugThread> threadList;
}