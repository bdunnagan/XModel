package org.xmodel.xaction.debug;

import java.util.Collection;
import java.util.Stack;
import java.util.concurrent.Semaphore;

import org.xmodel.BlockingDispatcher;
import org.xmodel.IModelObject;
import org.xmodel.ModelObject;
import org.xmodel.diff.DefaultXmlMatcher;
import org.xmodel.diff.XmlDiffer;
import org.xmodel.external.ExternalReference;
import org.xmodel.external.IExternalReference;
import org.xmodel.log.Log;
import org.xmodel.net.Server;
import org.xmodel.xaction.IXAction;
import org.xmodel.xaction.ScriptAction;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.StatefulContext;
import org.xmodel.xpath.variable.IVariableScope;

/**
 * A reference IDebugger implementation that uses an XPathServer to provide
 * access to the debugging operations and stack frame information.
 */
public class Debugger implements IDebugger
{
  public final static int defaultPort = 27700;
  
  public Debugger()
  {
    stack = new Stack<Frame>();
    semaphore = new Semaphore( 0);
    stepFrame = 1;
    debugRoot = new ModelObject( "debug");
    context = new StatefulContext( debugRoot);
    dispatcher = new BlockingDispatcher();
  }

  protected static class Frame
  {
    public Frame( IContext context, ScriptAction script)
    {
      this.context = context;
      this.script = script;
    }
    
    public IContext context;
    public ScriptAction script;
  }
  
  /**
   * Called when execution is paused.
   * @param context The execution context.
   * @param stack The stack.
   */
  protected void pause( IContext context, Stack<Frame> stack)
  {
    log.info( "debugger: pausing ...");
  }
  
  /**
   * Called when the current script is complete.
   */
  protected void resume()
  {
    log.info( "debugger: resuming ...");
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.debug.IDebugger#stepOver()
   */
  @Override
  public void stepOver()
  {
    synchronized( this)
    {
      stepFrame = currFrame;
      unblock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.debug.IDebugger#stepIn()
   */
  @Override
  public void stepIn()
  {
    synchronized( this)
    {
      stepFrame = currFrame + 1;
      unblock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.debug.IDebugger#stepOut()
   */
  @Override
  public void stepOut()
  {
    synchronized( this)
    {
      stepFrame = currFrame - 1;
      unblock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.debug.IDebugger#push(org.xmodel.xpath.expression.IContext, org.xmodel.xaction.ScriptAction)
   */
  @Override
  public void push( IContext context, ScriptAction script)
  {
    synchronized( this)
    {
      currFrame++;
      
      Frame frame = new Frame( context, script);
      stack.push( frame);
      
      debugRoot.addChild( createFrameElement( frame, null));
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.debug.IDebugger#run(org.xmodel.xpath.expression.IContext, org.xmodel.xaction.IXAction)
   */
  @Override
  public Object[] run( IContext context, IXAction action)
  {
    synchronized( this)
    {
      if ( stepFrame >= currFrame) 
      {
        Frame frame = stack.peek();
        
        IModelObject element = debugRoot.getChild( debugRoot.getNumberOfChildren() - 1);
        IModelObject revised = createFrameElement( frame, action);
        XmlDiffer differ = new XmlDiffer( new DefaultXmlMatcher( true));
        differ.diffAndApply( element, revised);
        
        pause( context, stack);
        block();
      }
      
      Object[] result = action.run( context);
      return result;
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.debug.IDebugger#pop()
   */
  @Override
  public void pop()
  {
    synchronized( this)
    {
      stack.pop();
      
      int depth = debugRoot.getNumberOfChildren();
      if ( depth > 0) debugRoot.removeChild( depth-1); 
        
      currFrame--;
      if ( currFrame == 0) resume();
    }
  }
  
  /**
   * Block the thread being debugged.
   */
  private void block()
  {
    if ( server == null)
    {
      try
      {
        server = new Server( "0.0.0.0", defaultPort, 60000);
        server.setContext( context);
        server.start();
      }
      catch( Exception e)
      {
        log.exception( e);
      }
    }

    try
    {
      server.pushDispatcher( dispatcher);    
      while( true)
      {
        dispatcher.process();
        if ( semaphore.tryAcquire()) break;
      }
    }
    finally
    {
      server.popDispatcher();
    }
  }

  /**
   * Unblock the thread being debugged.
   */
  private void unblock()
  {
    semaphore.release();
  }

  /**
   * Create an element representing the specified frame.
   * @param frame The frame.
   * @param action The next action to be executed.
   * @return Returns the new element.
   */
  private static IModelObject createFrameElement( Frame frame, IXAction action)
  {
    ModelObject element = new ModelObject( "frame", Integer.toString( frame.hashCode(), 16));
    
    // temporarily mark action
    if ( action != null) action.getDocument().getRoot().setAttribute( "debug:next", "");
    
    // script
    IModelObject scriptRoot = frame.script.getDocument().getRoot();
    IModelObject scriptClone = scriptRoot.cloneTree();
    scriptClone.setID( Integer.toString( frame.hashCode(), 16));
    element.getCreateChild( "script").addChild( scriptClone);
    
    // remove marker
    if ( action != null) action.getDocument().getRoot().removeAttribute( "debug:next");
    
    // variables
    IVariableScope scope = frame.context.getScope();
    Collection<String> vars = scope.getVariables();
    IModelObject varRoot = element.getCreateChild( "variables");
    for( String var: vars)
    {
      IExternalReference varElement = new ExternalReference( "variable");
      varElement.setID( var);
      varElement.setCachingPolicy( new ContextCachingPolicy( frame.context));
      varElement.setDirty( true);
      varRoot.addChild( varElement);
    }
    
    return element;
  }
  
  private static Log log = Log.getLog( "org.xmodel.xaction.debug");
  
  private Stack<Frame> stack;
  private int stepFrame;
  private int currFrame;
  private StatefulContext context;
  private IModelObject debugRoot;
  private Server server;
  private Semaphore semaphore;
  private BlockingDispatcher dispatcher;
}