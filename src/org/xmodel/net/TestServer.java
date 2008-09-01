package org.xmodel.net;

import org.xmodel.IModel;
import org.xmodel.IModelObject;
import org.xmodel.Xlate;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.Context;
import org.xmodel.xpath.expression.IExpression;

public class TestServer
{
  public static void main( String[] args) throws Exception
  {
    XmlIO xmlIO = new XmlIO();
    IModelObject root = xmlIO.read( TestServer.class.getResource( "test.xml"));
    IModel model = root.getModel();
    
    final BlockingDispatcher dispatcher = new BlockingDispatcher();
    model.setDispatcher( dispatcher);
    model.addRoot( "root", root);
    
    ModelServer server = new ModelServer( model);
    server.setQueryContext( new Context( root));
    server.start( 17310);

    final IExpression nodeExpr = XPath.createExpression( "collection('root')/ControllerTypeList[ 1]/ControllerTypeDefinition[ 1]/ControllerDefault");

    Thread thread = new Thread() {
      public void run()
      {
        try { Thread.sleep( 5000);} catch( Exception e) {}
        while( true)
        {
          try { Thread.sleep( 1000);} catch( Exception e) {}
          dispatcher.execute( new Runnable() {
            public void run()
            {
              IModelObject node = nodeExpr.queryFirst();
              node.setValue( Xlate.get( node, 0) + 1);
            }
          });
        }
      }
    };
    thread.start();
    
    while( true) dispatcher.process();
  }
  
  private static long t;
}
