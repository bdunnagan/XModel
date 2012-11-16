package org.xmodel.net;

import org.xmodel.IModelObject;
import org.xmodel.ModelListener;
import org.xmodel.concurrent.ThreadPoolContext;
import org.xmodel.concurrent.ThreadPoolDispatcher;
import org.xmodel.log.SLog;
import org.xmodel.net.bind.BindRequestProtocol.BindResult;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.StatefulContext;

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
    
    XioClient client = new XioClient( context, context);
    ConnectFuture future = client.connect( "localhost", 10000, new int[] { 1000, 2000, 3000});

    Thread.sleep( 2000);
    
    XioServer server = new XioServer( context, context);
    server.start( "localhost", 10000);
    
    future.await();
    final BindResult result = client.bind( true, "$list", 100);

    result.element.getChild( 1).addModelListener( new ModelListener() {
      public void notifyChange( IModelObject object, String attrName, Object newValue, Object oldValue)
      {
        SLog.info( NettyTest.class, XmlIO.toString( result.element));
      }
    });
    
    SLog.info( NettyTest.class, XmlIO.toString( result.element));
    list.getChild( 1).setValue( "CHANGE!");
    
    Thread.sleep( 100);
    
    xml =
      "<script name='Test'>" +
      "  <print>'Hi, Client!'</print>" +
      "</script>";
    
    StatefulContext localContext = new StatefulContext();
    client.execute( localContext, new String[ 0], new XmlIO().read( xml), 1000);
    
    client.close();
    server.stop();
    
    context.getModel().getDispatcher().shutdown( false);
  }
}
