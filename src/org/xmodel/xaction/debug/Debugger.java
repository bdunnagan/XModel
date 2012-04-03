package org.xmodel.xaction.debug;

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
  
  public static enum Operation { stepOver, stepIn, stepOut, pause, resume};
  
  public Debugger()
  {
    semaphore = new Semaphore( 0);
    stepFrame = -1;
    
    stack = new ModelObject( "stack");
    Xlate.set( stack, "thread", Thread.currentThread().getName());
  }

  public synchronized IModelObject getStack()
  {
    return stack.cloneTree();
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

  public synchronized void pause()
  {
    stepFrame = currFrame + 1;
  }
  
  public synchronized void resume()
  {
    stepFrame = -1;
    unblock();
  }

  public synchronized void push( IContext context, ScriptAction action)
  {
    currFrame++;

    IModelObject newFrame = new ModelObject( "frame");
    
    IModelObject parent = (frame != null)? frame: stack;
    parent.addChild( newFrame);
    
    frame = newFrame;
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

  private int stepFrame;
  private int currFrame;
  private IModelObject stack;
  private IModelObject frame;
  private Semaphore semaphore;
}