package org.xmodel.net.test;

import java.util.ArrayList;
import java.util.List;

import org.xmodel.IModelObject;
import org.xmodel.ManualDispatcher;
import org.xmodel.ModelObject;
import org.xmodel.caching.FileSystemCachingPolicy;
import org.xmodel.external.ExternalReference;
import org.xmodel.log.Log;
import org.xmodel.net.NetworkCachingPolicy;
import org.xmodel.net.Server;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;

public class NetTest
{
  public void startServer( final int port)
  {
    Runnable runnable = new Runnable() {
      public void run()
      {
        try
        {
          Server server = new Server( "127.0.0.1", port, 100000, true);
          server.start( true);
          
          ExternalReference serverModel = new ExternalReference( "server");
          serverModel.setAttribute( "path", "/Users/bdunnagan/Documents/CornerstoneWorkspace/CorePlugin/xml");
          serverModel.setCachingPolicy( new FileSystemCachingPolicy());
          serverModel.setDirty( true);
          
          ManualDispatcher dispatcher = new ManualDispatcher();
          serverModel.getModel().setDispatcher( dispatcher);
          
          server.setServerContext( new StatefulContext( serverModel));
          
          while( true)
          {
            dispatcher.process();
            Thread.sleep( 100);
          }
        }
        catch( Exception e)
        {
          log.exception( e);
        }
      }
    };
    
    Thread thread = new Thread( runnable, "ServerModel");
    thread.setDaemon( true);
    thread.start();
  }
  
  public IModelObject createClient( int port)
  {
    ExternalReference clientModel = new ExternalReference( "client");
    clientModel.setAttribute( "host", "127.0.0.1");
    clientModel.setAttribute( "port", 9000);
    clientModel.setAttribute( "timeout", 100000);
    clientModel.setAttribute( "xpath", ".");
    
    if ( cachingPolicy == null)
    {
      cachingPolicy = new NetworkCachingPolicy();
      ModelObject annotation = new ModelObject( "annotation");
      annotation.setAttribute( "host", "127.0.0.1");
      annotation.setAttribute( "port", 9000);
      annotation.setAttribute( "timeout", 100000);
      annotation.setAttribute( "xpath", ".");
      cachingPolicy.configure( new StatefulContext(), annotation);
    }
    
    clientModel.setCachingPolicy( cachingPolicy);
    clientModel.setDirty( true);
    
    return clientModel;
  }
  
  public void test1() throws Exception
  {
    startServer( 9000);
    Thread.sleep( 1000);

    List<IModelObject> clientModels = new ArrayList<IModelObject>();
    for( int i=0; i<2; i++)
    {
      IModelObject clientModel = createClient( 9000);
  
      IExpression testExpr = XPath.createExpression( "common/AddressGroupTree.xml/*");
      IModelObject element = testExpr.queryFirst( clientModel);
      System.out.println( element.getType());
      
      clientModels.add( clientModel);
    }
    
    for( int i=0; i<2; i++)
    {
      IModelObject clientModel = clientModels.get( i);
      clientModel.getChild( 1).setValue( "FLUFF!");
    }    
  }
  
  public static void main( String[] args) throws Exception
  {
    NetTest test = new NetTest();
    test.test1();
  }
  
  private Log log = new Log( "org.xmodel.net.test");
  private NetworkCachingPolicy cachingPolicy;
}
