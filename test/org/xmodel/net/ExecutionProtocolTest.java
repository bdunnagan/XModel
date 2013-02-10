package org.xmodel.net;

import static org.junit.Assert.*;
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
public class ExecutionProtocolTest
{
  public final static String address = "localhost";
  public final static int port = 10000;
  public final static int timeout = 30000;
  public final static int largePayloadCount = 100000;
  
  @Before public void start() throws IOException
  {
    GlobalSettings.getInstance().getModel().setExecutor( new ThreadPoolExecutor( "server-model", 1));
    serverContext = new StatefulContext();
    server = new XioServer( serverContext);
    server.start( address, port);
  }
  
  @After public void shutdown() throws IOException
  {
    for( XioClient client: clients)
    {
      client.close();
      client = null;
    }
    clients.clear();
    clients = null;
    
    server.stop();
    server = null;
  }
  
  @Test public void asyncExecute() throws Exception
  {
//    Log.getLog( ExecutionRequestProtocol.class).setLevel( Log.all);
//    Log.getLog( ExecutionResponseProtocol.class).setLevel( Log.all);
    
    GlobalSettings.getInstance().getModel().setExecutor( new ThreadPoolExecutor( "client-model", 1));
    
    createClients( 1);

    final XioClient client = clients.get( 0);
    client.connect( address, port).await();
    
    final int count = 10000;
    final Semaphore semaphore = new Semaphore( 0);
    
    XioCallback cb = new XioCallback( count, semaphore);

    String xml = 
      "<script>" +
      "  <lock on='$value'>" +
      "    <set target='$value'>$value + 1</set>" +
      "    <return>string( $value)</return>" +
      "  </lock>" +
      "</script>";

    try
    {
      serverContext.getLock().writeLock().lock();
      IModelObject valueNode = new ModelObject( "value");
      valueNode.setValue( -1);
      serverContext.set( "value", valueNode);
      serverContext.getLock().writeLock().unlock();
      
      IModelObject script = new XmlIO().read( xml);
      StatefulContext context = new StatefulContext();
      
      for( int i=0; i<count; i++)
      {
        client.execute( context, new String[ 0], script, cb, 600000);
        assertTrue( cb.failed == -1);
      }
    }
    catch( Exception e)
    {
      e.printStackTrace( System.err);
    }
    
    //System.out.println( "Finished sending ...");
    
    semaphore.acquireUninterruptibly();
    //System.out.println( "Done.");
  }
  
  @Test public void largeExecutePayload() throws Exception
  {
    createClients( 1);

    String scriptXml = "" +
        "<script>" +
        "  <return>count( $payload/*)</return>" +
        "</script>";
    
    IModelObject payload = new ModelObject( "payload");
    for( int j=0; j<largePayloadCount; j++)
    {
      IModelObject child = new ModelObject( "payload");
      payload.addChild( child);
    }
  
    StatefulContext context = new StatefulContext();
    context.set( "payload", payload);
    
    XioClient client = clients.get( 0);
    client.connect( address, port).await();
    
    IModelObject script = new XmlIO().read( scriptXml);
    Object[] result = client.execute( context, new String[] { "payload"}, script, timeout);
    
    int count = ((Number)result[ 0]).intValue();
    assertTrue( String.format( "Payload count is %d, should be %d", count, largePayloadCount), count == largePayloadCount);
  }
    
  private void createClients( int count) throws IOException
  {
    clients = new ArrayList<XioClient>();
    for( int i=0; i<count; i++)
    {
      XioClient client = new XioClient();
      clients.add( client);
    }
  }
  
  private static class XioCallback implements IXioCallback
  {
    public XioCallback( int count, Semaphore semaphore)
    {
      this.count = count;
      this.semaphore = semaphore;
      this.failed = -1;
    }
    
    public void onComplete( IContext context)
    {
    }
    public void onSuccess( IContext context, Object[] results)
    {
      if ( failed >= 0) return;
      
      //try { Thread.sleep( 100);} catch( Exception e) {}
      int i = (int)Double.parseDouble( results[ 0].toString());
      
      if ( i != expect) failed = i;
      
      //if ( (i % 1000) == 0) System.out.printf( "%d, %d\n", i, expect);
      if ( expect == (count - 1)) semaphore.release();
      
      expect++;
    }
    public void onError( IContext context, String error)
    {
    }
    
    private int count;
    private int expect;
    private Semaphore semaphore;
    public volatile int failed;
  }

  private StatefulContext serverContext;
  private XioServer server;
  private List<XioClient> clients;
}
