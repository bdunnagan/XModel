package org.xmodel.net;

import org.xmodel.IModelObject;
import org.xmodel.ModelListener;
import org.xmodel.concurrent.ThreadPoolContext;
import org.xmodel.concurrent.ThreadPoolDispatcher;
import org.xmodel.net.bind.BindRequestProtocol.BindResult;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.expression.IContext;

public class NettyTest
{
  public static void main( String[] args) throws Exception
  {
    IContext context = new ThreadPoolContext( new ThreadPoolDispatcher( 1));
    
    String xml = 
      "<list>" +
      "  <item id='1'>Item 1</item>" +
      "  <item id='2'>Item 2</item>" +
      "  <item id='3'>Item 3</item>" +
      "</list>";

    IModelObject list = new XmlIO().read( xml);
    context.set( "list", list);
    
    Server server = new Server( context, context);
    server.start( "localhost", 10000);
    
    Client client = new Client( context, context);
    client.connect( "localhost", 10000).await();
    
    final BindResult result = client.bind( true, "$list", 100);
    System.out.println( XmlIO.toString( result.element));

    result.element.getChild( 1).addModelListener( new ModelListener() {
      public void notifyChange( IModelObject object, String attrName, Object newValue, Object oldValue)
      {
        System.out.println( XmlIO.toString( result.element));
      }
    });
    
    list.getChild( 1).setValue( "CHANGE!");
    
    Thread.sleep( 100);
    
    client.close();
    server.stop();
    
    context.getModel().getDispatcher().shutdown( false);
  }
}
