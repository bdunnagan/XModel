package org.xmodel.external;

import org.xmodel.BlockingDispatcher;
import org.xmodel.IModelObject;
import org.xmodel.ModelObject;
import org.xmodel.net.Server;
import org.xmodel.xaction.IXAction;
import org.xmodel.xaction.XActionDocument;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;

public class GCTest
{
  public static void main( String[] args) throws Exception
  {
    IModelObject serverRoot = new ModelObject( "server");
    serverRoot.getCreateChild( "child");
    
    server = new Server( "localhost", 33333);
    server.setServerContext( new StatefulContext( serverRoot));
    server.start( true);
    
    Thread thread = new Thread( "Server") {
      public void run()
      {
        try
        {
          BlockingDispatcher dispatcher = (BlockingDispatcher)server.getDispatcher();
          while( dispatcher.process());
        }
        catch( Exception e)
        {
          e.printStackTrace( System.err);
        }
      }
    };
    
    thread.start();
    
    IExpression refExpr = XPath.createExpression( "$ref");
    
    String xml = 
        "<script>" +
        "  <create var='ref'>" +
        "    <ref>" +
        "      <extern:cache class=\"org.xmodel.net.NetworkCachingPolicy\" dirty=\"true\">" + 
        "        <host>localhost</host>" + 
        "        <port>33333</port>" + 
        "        <timeout>30000</timeout>" + 
        "        <query>.</query>" + 
        "      </extern:cache>" +
        "    </ref>" + 
        "  </create>" +
        "</script>";
    
    clientContext = new StatefulContext();
    
    XActionDocument doc = new XActionDocument( new XmlIO().read( xml));
    script = doc.createScript();

    for( int i=0; i<10000000; i++)
    {
      System.out.println( i);

      script.run( clientContext);
      
      IExternalReference ref = (IExternalReference)refExpr.queryFirst( clientContext);
      ref.getChildren();
    }    
  }
  
  private static StatefulContext clientContext;
  private static IXAction script;
  private static Server server;
}
