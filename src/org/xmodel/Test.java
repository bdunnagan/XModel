package org.xmodel;

import java.util.List;

import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.ExpressionListener;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;

public class Test extends ExpressionListener implements IPathListener
{
  public void notifyAdd( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    System.out.printf( "+");
    for( IModelObject node: nodes)
      System.out.printf( "%s ", node.getID());
    System.out.println( "");
  }

  public void notifyRemove( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    System.out.printf( "-");
    for( IModelObject node: nodes)
      System.out.printf( "%s ", node.getID());
    System.out.println( "");
  }

  public void notifyAdd( IContext context, IPath path, int pathIndex, List<IModelObject> nodes)
  {
    if ( pathIndex < path.length()) return;
    
    System.out.printf( "+");
    for( IModelObject node: nodes)
      System.out.printf( "%s ", node.getID());
    System.out.println( "");
  }

  public void notifyRemove( IContext context, IPath path, int pathIndex, List<IModelObject> nodes)
  {
    if ( pathIndex < path.length()) return;
    
    System.out.printf( "-");
    for( IModelObject node: nodes)
      System.out.printf( "%s ", node.getID());
    System.out.println( "");
  }

  public void notifyChange( IContext context, IPath path, int pathIndex)
  {
  }

  public void run() throws Exception
  {
    //IPath path = XPath.createPath( "b[ @id = '1']");
    IExpression expr = XPath.createExpression( "b[ @id = '1']");
    
    XmlIO xmlIO = new XmlIO();
    String xml = 
      "<a>" +
      "  <b id='1'/>" +
      "  <b id='2'/>" +
      "</a>";
    
    IModelObject a = xmlIO.read( xml);

    StatefulContext context = new StatefulContext( a);
    //path.addPathListener( context, this);
    expr.addListener( context, this);
    
    IModelObject b = a.removeChild( 0);
    a.addChild( b, 0);
  }
  
  public static void main( String[] args) throws Exception
  {
    Test test = new Test();
    test.run();
  }
}
