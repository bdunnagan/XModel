package org.xmodel.xaction.debug;

import java.util.Collection;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.Semaphore;

import org.xmodel.IModelObject;
import org.xmodel.ModelObject;
import org.xmodel.Reference;
import org.xmodel.external.ExternalReference;
import org.xmodel.external.IExternalReference;
import org.xmodel.log.Log;
import org.xmodel.net.XPathServer;
import org.xmodel.util.Identifier;
import org.xmodel.xaction.IXAction;
import org.xmodel.xaction.ScriptAction;
import org.xmodel.xaction.XAction;
import org.xmodel.xaction.XActionDocument;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.StatefulContext;
import org.xmodel.xpath.variable.IVariableScope;

/**
 * A base class for IDebugger implementations.
 */
public abstract class Debugger implements IDebugger
{
  public Debugger()
  {
    semaphore = new Semaphore( 0);
    stack = new Stack<Frame>();
    stepFrame = 1;
    random = new Random();
    context = new StatefulContext( new ModelObject( "debug"));
  }

  protected static class Frame
  {
    public Frame( IContext context, IXAction action)
    {
      this.context = context;
      this.action = action;
    }
    
    public IContext context;
    public IXAction action;
  }
  
  /**
   * Called when execution is paused.
   * @param context The execution context.
   * @param stack The stack.
   */
  protected abstract void pause( IContext context, Stack<Frame> stack);
  
  /**
   * Called when the current script is complete.
   */
  protected abstract void resume();
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.debug.IDebugger#stepOver()
   */
  @Override
  public void stepOver()
  {
    stepFrame = currFrame;
    unblock();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.debug.IDebugger#stepIn()
   */
  @Override
  public void stepIn()
  {
    stepFrame = currFrame + 1;
    unblock();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.debug.IDebugger#stepOut()
   */
  @Override
  public void stepOut()
  {
    stepFrame = currFrame - 1;
    unblock();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.debug.IDebugger#push(org.xmodel.xpath.expression.IContext, org.xmodel.xaction.ScriptAction)
   */
  @Override
  public void push( IContext context, ScriptAction script)
  {
    currFrame++;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.debug.IDebugger#run(org.xmodel.xpath.expression.IContext, org.xmodel.xaction.IXAction)
   */
  @Override
  public Object[] run( IContext context, IXAction action)
  {
    Frame frame = new Frame( context, action);
    stack.push( frame);
    if ( stepFrame >= currFrame) 
    {
      context.getObject().addChild( createFrameElement( frame), 0);
      pause( context, stack);
      block();
    }
    
    Object[] result = action.run( context);
    stack.pop();
    
    return result;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.debug.IDebugger#pop()
   */
  @Override
  public void pop()
  {
    context.getObject().removeChild( 0); 
    currFrame--;
    if ( currFrame == 0) resume();
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
        server = new XPathServer();
        server.setContext( context);
        server.start( "127.0.0.1", 27700);
      }
      catch( Exception e)
      {
        log.exception( e);
      }
    }
    
    try { semaphore.acquire();} catch( InterruptedException e) {}
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
  @SuppressWarnings("unchecked")
  private IModelObject createFrameElement( Frame frame)
  {
    ModelObject element = new ModelObject( "frame", Identifier.generate( random, 10));
    
    // xaction
    IModelObject xaction = frame.action.getDocument().getRoot();
    element.getCreateChild( "action").addChild( new Reference( xaction));
    
    // variables
    IVariableScope scope = frame.context.getScope();
    Collection<String> variables = scope.getVariables();
    IModelObject varRoot = element.getCreateChild( "variables");
    for( String variable: variables)
    {
      IExternalReference varElement = new ExternalReference( "variable");
      varElement.setID( variable);
      varElement.setCachingPolicy( new ContextCachingPolicy( frame.context));
      varElement.setDirty( true);
      varRoot.addChild( varElement);
    }
    
    return element;
  }
  
  private static Log log = Log.getLog( "org.xmodel.xaction.debug");
  
  private Semaphore semaphore;
  private Stack<Frame> stack;
  private int stepFrame;
  private int currFrame;
  private Random random;
  private StatefulContext context;
  private XPathServer server;
  
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