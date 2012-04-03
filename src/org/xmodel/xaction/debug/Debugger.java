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
    stepFrame = 1;
    stack = new ModelObject( "stack");
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

  public void resume()
  {
    stepFrame = -1;
    unblock();
  }

  public synchronized void push( IContext context, ScriptAction action)
  {
    currFrame++;
    
    // set stack ID to name of thread
    Xlate.set( stack, "thread", Thread.currentThread().getName());

    // build frame
    IModelObject parent = (frame != null)? frame: stack;
    frame = new ModelObject( "frame");
    
    // location
    IPath path = ModelAlgorithms.createIdentityPath( action.getDocument().getRoot(), true);
    Xlate.set( frame, "location", path.toString());
    
    // store variable names in scope
    for( String varName: context.getScope().getVariables())
    {
      ModelObject varNode = new ModelObject( "var");
      varNode.setValue( varName);
      frame.addChild( varNode);
    }
    
    parent.addChild( frame);
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
    
    // remove current frame
    if ( frame != null && frame != stack) frame.removeFromParent();
  }
  
  private void block()
  {
    try 
    { 
      semaphore.acquire();
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

  private int stepFrame;
  private int currFrame;
  private IModelObject stack;
  private IModelObject frame;
  private Semaphore semaphore;
}