package org.xmodel.xaction.debug;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import org.xmodel.IModelObject;
import org.xmodel.ModelObject;
import org.xmodel.ThreadPoolDispatcher;
import org.xmodel.Xlate;
import org.xmodel.log.SLog;
import org.xmodel.net.Server;
import org.xmodel.xaction.IXAction;
import org.xmodel.xaction.ScriptAction;
import org.xmodel.xml.IXmlIO.Style;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.StatefulContext;
import org.xmodel.xpath.variable.IVariableScope;

/**
 * A reference IDebugger implementation that uses an XPathServer to provide
 * access to the debugging operations and stack frame information.
 */
public class Debugger
{
  public final static String debugProperty = "xaction:debug";
  
  public static enum Operation { sync, stepOver, stepIn, stepOut, pause, resume};
  
  public Debugger()
  {
    threads = new ThreadLocal<DebugThread>();
    threadList = new ArrayList<DebugThread>();
    lock = new Semaphore( 0);
    
    try
    {
      int port = Integer.parseInt( System.getProperty( debugProperty));
      server = new Server( "localhost", port, Integer.MAX_VALUE);
      server.setDispatcher( new ThreadPoolDispatcher( Executors.newFixedThreadPool( 1)));
      server.setServerContext( new StatefulContext());
      server.start( true);
    }
    catch( Exception e)
    {
      SLog.exception( this, e);
    }
  }    

  public synchronized void stepOver( String threadName)
  {
    DebugThread thread = getDebugThread( threadName);
    thread.op = Operation.stepOver;
    unblock();
  }

  public synchronized void stepIn( String threadName)
  {
    DebugThread thread = getDebugThread( threadName);
    thread.op = Operation.stepIn;
    unblock();
  }

  public synchronized void stepOut( String threadName)
  {
    DebugThread thread = getDebugThread( threadName);
    thread.op = Operation.stepOut;
    unblock();
  }

  public synchronized void pause( String threadName)
  {
    DebugThread thread = getDebugThread( threadName);
    thread.op = Operation.pause;
  }
  
  public void breakpoint()
  {
    synchronized( this)
    {
      DebugThread thread = getDebugThread();
      thread.op = Operation.pause;
    }
    
    block();
  }
  
  public synchronized void resume( String threadName)
  {
    DebugThread thread = getDebugThread( threadName);
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

  public void pop()
  {
    boolean end = false;
    
    synchronized( this)
    {
      DebugThread thread = getDebugThread();
      thread.frames.remove( thread.frames.size() - 1);
      
      if ( thread.frames.size() == 0)
      {
        threads.set( null);
        threadList.remove( thread);
        if ( thread.op == Operation.pause || thread.op == Operation.stepOver) end = true;
      }
    }
    
    if ( end) block();
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
      config.clearModel();
      config.removeAttribute( "xaction");
      config.removeAttribute( "xm:compiled");
      return XmlIO.write( Style.printable, config);
    }
        
    public IContext context;
    public IXAction action;
  }
  
  private ThreadLocal<DebugThread> threads;
  private List<DebugThread> threadList;
  private Semaphore lock;
  private Server server;
}