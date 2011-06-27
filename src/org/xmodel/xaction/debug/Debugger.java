package org.xmodel.xaction.debug;

import java.util.Collection;
import java.util.Stack;
import java.util.concurrent.Semaphore;

import org.xmodel.BlockingDispatcher;
import org.xmodel.IModelObject;
import org.xmodel.ModelObject;
import org.xmodel.Reference;
import org.xmodel.diff.XmlDiffer;
import org.xmodel.external.ExternalReference;
import org.xmodel.external.IExternalReference;
import org.xmodel.log.Log;
import org.xmodel.net.Server;
import org.xmodel.xaction.IXAction;
import org.xmodel.xaction.ScriptAction;
import org.xmodel.xaction.XAction;
import org.xmodel.xaction.XActionDocument;
import org.xmodel.xml.XmlIO;
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
    public Frame( IContext context)
    {
      this.context = context;
    }
    
    public IContext context;
    public IXAction action;
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
      
      Frame frame = new Frame( context);
      stack.push( frame);
      
      debugRoot.addChild( createFrameElement( frame));
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
        frame.action = action;
        
        IModelObject element = debugRoot.getChild( debugRoot.getNumberOfChildren() - 1);
        IModelObject revised = createFrameElement( frame);
        XmlDiffer differ = new XmlDiffer();
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
        server = new Server( "0.0.0.0", defaultPort);
        server.setContext( context);
        server.setDispatcher( dispatcher);
        server.start();
      }
      catch( Exception e)
      {
        log.exception( e);
      }
    }

    while( true)
    {
      dispatcher.process();
      if ( semaphore.tryAcquire()) break;
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
   * @return Returns the new element.
   */
  private static IModelObject createFrameElement( Frame frame)
  {
    ModelObject element = new ModelObject( "frame", Integer.toString( frame.hashCode()));
    
    // xaction
    if ( frame.action != null)
    {
      IModelObject xaction = frame.action.getDocument().getRoot();
      element.getCreateChild( "action").addChild( xaction.cloneTree());
    }
    
    // variables
    IVariableScope scope = frame.context.getScope();
    Collection<String> vars = scope.getVariables();
    IModelObject varRoot = element.getCreateChild( "vars");
    for( String var: vars)
    {
      IExternalReference varElement = new ExternalReference( "var");
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
  
  public static void main( String[] args) throws Exception
  {
    final String xml = "" +
      "<script>" +
      "  <assign name=\"x\">'1a'</assign>" +
      "  <assign name=\"x\">'1b'</assign>" +
      "  <invoke>$xml2</invoke>" +
      "  <script>" +
      "    <assign name=\"x\">'2a'</assign>" +
      "    <assign name=\"x\">'2b'</assign>" +
      "    <script>" +
      "      <assign name=\"x\">'3a'</assign>" +
      "      <assign name=\"x\">'3b'</assign>" +
      "    </script>" +
      "    <assign name=\"x\">'2c'</assign>" +
      "  </script>" +
      "  <assign name=\"x\">'1c'</assign>" +
      "</script>";
    
    final String xml2 = "" +
      "<script>" +
      "  <assign name=\"i\">i1</assign>" +
      "  <assign name=\"i\">i2</assign>" +
      "</script>";
    
    final Debugger debugger = new Debugger() {
      protected void pause( IContext context, Stack<Frame> stack)
      {
        for( int i=0; i<stack.size(); i++)
        {
          System.out.printf( "%d %s", i+1, stack.get( i).action);
        }
      }
      protected void resume()
      {
        System.exit( 0);
      }
    };
    
    Thread thread = new Thread( new Runnable() {
      public void run()
      {
        try
        {
          XAction.setDebugger( debugger);

          XmlIO xmlIO = new XmlIO();
          IModelObject node1 = xmlIO.read( xml);
          XActionDocument doc = new XActionDocument( node1);
          ScriptAction script = doc.createScript();
          StatefulContext context = new StatefulContext( node1);
          IModelObject node2 = xmlIO.read( xml2);
          context.set( "xml2", node2);
          script.run( context);
        }
        catch( Exception e)
        {
          e.printStackTrace( System.err);
        }
      }
    });
    
    thread.start();
    
    while( true)
    {
      int c = System.in.read();
      if ( c == 'i') debugger.stepIn();
      else if ( c == 'o') debugger.stepOut();
      else if ( c == 's') debugger.stepOver();
    }
  }
}