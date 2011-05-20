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
    
    debugger.step();
    debugger.step();
  }
  
  public static String xml = "" +
  		"<script>" +
  		"  <create assign=\"x\" name=\"'x'\"/>" +
  		"  <set source=\"Fred\">$x</set>" +
  		"</script>";
}

class Debugger implements IDebugger
{
  public Debugger()
  {
    stack = new ArrayList<Frame>();
  }
  
  public Object[] step()
  {
    if ( stack.size() == 0) return null;
    System.out.println( "Stepping...");
    return null;
  }

  public Object[] run( IContext context, IXAction script, List<IXAction> actions)
  {
    if ( stack.size() == 0)
    {
      Frame frame = new Frame();
      frame.context = context;
      frame.actions = actions;
      frame.index = 0;
      stack.add( frame);
      index = 0;
    }
    else
    {
      Frame frame = stack.get( index++);
      assert( frame.script == script);
      
      IXAction action = frame.actions.get( frame.index++);
      frame.result = action.run( frame.context);
      
      // throw here?
      return frame.result;
    }
    return null;
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
