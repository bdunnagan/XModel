package org.xmodel.net;

import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xmodel.GlobalSettings;
import org.xmodel.IModelObject;
import org.xmodel.ModelObject;
import org.xmodel.concurrent.ThreadPoolExecutor;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.StatefulContext;

/**
 * Test case for many clients accessing a single server.
 */
public class XioClientPoolTest
{
  public final static String address = "localhost";
  public final static int port = 10000;
  public final static int timeout = 30000;
  
  @Before public void start() throws IOException
  {
    GlobalSettings.getInstance().setDefaultExecutor( new ThreadPoolExecutor( "server-model", 10));
    serverContext = new StatefulContext();
    server = new XioServer( serverContext);
    server.start( address, port);
  }
  
  @After public void shutdown() throws IOException
  {
    server.stop();
    server = null;
  }
  
  @Test public void asyncExecute() throws Exception
  {
//    Log.getLog( ExecutionRequestProtocol.class).setLevel( Log.all);
//    Log.getLog( ExecutionResponseProtocol.class).setLevel( Log.all);
    
    GlobalSettings.getInstance().setDefaultExecutor( new ThreadPoolExecutor( "client-model", 10));
    
    createClients( 1);

    final XioClient client = clients.get( 0);
    client.connect( address, port).await();
    
    final int count = 10000;
    final Semaphore semaphore = new Semaphore( 0);
    
    final IXioCallback cb = new IXioCallback() {
      public void onComplete( IContext context)
      {
      }
      public void onSuccess( IContext context, Object[] results)
      {
        //try { Thread.sleep( 100);} catch( Exception e) {}
        int i = (int)Double.parseDouble( results[ 0].toString());
        if ( (i % 1000) == 0)
        {
          System.out.printf( "%d\n", i);
        }
        
        if ( i == (count - 1))
          semaphore.release();
      }
      public void onError( IContext context, String error)
      {
      }
    };

    String xml = 
      "<script>" +
      "  <lock on='$value'>" +
      "    <set target='$value'>$value + 1</set>" +
      "    <return>string( $value)</return>" +
      "  </lock>" +
      "</script>";

    try
    {
      synchronized( serverContext)
      {
        IModelObject valueNode = new ModelObject( "value");
        valueNode.setValue( -1);
        serverContext.set( "value", valueNode);
      }
      
      IModelObject script = new XmlIO().read( xml);
      StatefulContext context = new StatefulContext();
      
      for( int i=0; i<count; i++)
      {
        client.execute( context, i, new String[ 0], script, cb, 600000);
      }
    }
    catch( Exception e)
    {
      e.printStackTrace( System.err);
    }
    
    System.out.println( "Finished sending ...");
    
    semaphore.acquireUninterruptibly();
    System.out.println( "Done.");
  }
  
  private StatefulContext serverContext;
  private XioServer server;
  private XioClientPool pool;
}
