package org.xmodel.xaction.debug;

import java.util.ArrayList;
import java.util.List;

import org.xmodel.IModelObject;
import org.xmodel.xaction.IXAction;
import org.xmodel.xaction.ScriptAction;
import org.xmodel.xaction.XAction;
import org.xmodel.xaction.XActionDocument;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.StatefulContext;

public class Main
{
  public static void main( String[] args) throws Exception
  {
    Debugger debugger = new Debugger();
    XAction.setDebugger( debugger);

    XmlIO xmlIO = new XmlIO();
    IModelObject node = xmlIO.read( xml);
    XActionDocument doc = new XActionDocument( node);
    ScriptAction script = doc.createScript();
    StatefulContext context = new StatefulContext( node);
    script.run( context);
    
    while( debugger.step());
  }
  
  public static String xml = "" +
  		"<script>" +
  		"  <create assign=\"x\">" +
  		"    <template>" +
  		"      <x id=\"\"/>" +
  		"    </template>" +
  		"    <id>@id</id>" +
  		"  </create>" +
  		"  <set source=\"Fred\">$x</set>" +
  		"</script>";
}

class Debugger implements IDebugger
{
  public Debugger()
  {
    stack = new ArrayList<Frame>();
  }
  
  public boolean step()
  {
    if ( stack.size() == 0) return false;
    System.out.println( "Stepping...");
    try
    {
      index = 0;
      Frame frame = stack.get( 0);
      frame.script.run( frame.context);
    }
    catch( DebuggingException e)
    {
    }
    return true;
  }

  public Object[] run( IContext context, IXAction script, List<IXAction> actions)
  {
    if ( stack.size() == 0)
    {
      Frame frame = new Frame();
      frame.context = context;
      frame.script = script;
      frame.actions = actions;
      frame.index = 0;
      stack.add( frame);
      
      // stop before executing first action
      return null;
    }
    else if ( index < (stack.size() - 1))
    {
      // continue re-building stack frame
      Frame frame = stack.get( index++);
      IXAction action = frame.actions.get( frame.index);
      frame.result = action.run( frame.context);
    }
    else
    {
      Frame frame = stack.get( index);
      if ( script == frame.script)
      {
        // execute next instruction
        IXAction action = frame.actions.get( frame.index);
        System.out.println( "EXEC: "+action);
        frame.result = action.run( frame.context);
        if ( frame.index == frame.actions.size())
        {
          stack.remove( index--);
          frame = stack.get( index);
          frame.index++;
        }
      }
      else
      {
        frame = new Frame();
        frame.context = context;
        frame.script = script;
        frame.actions = actions;
        frame.index = 0;
        stack.add( frame);
      }
    }
    
    throw new DebuggingException();
  }
  
  private class Frame
  {
    public IContext context;
    public IXAction script;
    public List<IXAction> actions;
    public int index;
    public Object[] result;
  }
  
  private List<Frame> stack;
  private int index;
}
