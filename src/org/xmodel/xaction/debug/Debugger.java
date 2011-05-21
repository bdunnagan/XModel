package org.xmodel.xaction.debug;

import java.util.Stack;
import java.util.concurrent.Semaphore;

import org.xmodel.IModelObject;
import org.xmodel.xaction.IXAction;
import org.xmodel.xaction.ScriptAction;
import org.xmodel.xaction.XAction;
import org.xmodel.xaction.XActionDocument;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.StatefulContext;

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
    stack.push( new Frame( context, action));
    if ( stepFrame >= currFrame) 
    {
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
    currFrame--;
    if ( currFrame == 0) resume();
  }
  
  /**
   * Block the thread being debugged.
   */
  private void block()
  {
    try { semaphore.acquire();} catch( InterruptedException e) {}
  }

  /**
   * Unblock the thread being debugged.
   */
  private void unblock()
  {
    semaphore.release();
  }
  
  private Semaphore semaphore;
  private Stack<Frame> stack;
  private int stepFrame;
  private int currFrame;
  
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