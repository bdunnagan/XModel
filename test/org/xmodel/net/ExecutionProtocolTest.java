package org.xmodel.net;

import static org.junit.Assert.assertFalse;
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
import org.xmodel.log.Log;
import org.xmodel.net.execution.ExecutionRequestProtocol;
import org.xmodel.net.execution.ExecutionResponseProtocol;
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
    GlobalSettings.getInstance().setDefaultExecutor( new ThreadPoolExecutor( "server-model", 1));
    serverContext = new StatefulContext();
    server = new XioServer( serverContext);
    server.start( address, port);
  }
  
  @After public void shutdown() throws IOException
  {
    if ( clients != null)
    {
      for( XioClient client: clients)
      {
        client.close();
        client = null;
      }
      clients.clear();
      clients = null;
    }
    
    server.stop();
    server = null;
  }
  
  @Test public void asyncExecute() throws Exception
  {
//    Log.getLog( ExecutionRequestProtocol.class).setLevel( Log.all);
//    Log.getLog( ExecutionResponseProtocol.class).setLevel( Log.all);
    
    GlobalSettings.getInstance().setDefaultExecutor( new ThreadPoolExecutor( "client-model", 1));
    
    createClients( 1);

    final XioClient client = clients.get( 0);
    client.connect( address, port).await();
    
    final int count = 10000;
    final Semaphore semaphore = new Semaphore( 0);
    
    XioCallback cb = new XioCallback( count, semaphore);

    String xml = 
      "<script>" +
      "  <mutex on='$value'>" +
      "    <set target='$value'>$value + 1</set>" +
      "    <return>string( $value)</return>" +
      "  </mutex>" +
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
    
  @Test
  public void cancelTest() throws Exception
  {
    Log.getLog( ExecutionRequestProtocol.class).setLevel( Log.all);
    Log.getLog( ExecutionResponseProtocol.class).setLevel( Log.all);
    
    String xml = 
      "<script>" +
      "  <sleep>1000</sleep>" +
      "</script>";
    
    IModelObject element = new XmlIO().read( xml);
    
    XioClient client = new XioClient( GlobalSettings.getInstance().getDefaultExecutor());
    assertTrue( "Failed to connect", client.connect( "localhost", port).await( 1000));

    class Callback implements IXioCallback
    {
      public void onComplete( IContext context)
      {
        complete = true;
      }
      public void onSuccess( IContext context, Object[] results)
      {
        success = true;
      }
      public void onError( IContext context, String error)
      {
      }
      
      public boolean success;
      public boolean complete;
    };

    Callback callback = new Callback();
    StatefulContext context = new StatefulContext();
    client.execute( context, 1, new String[ 0], element, callback, 1200);
    client.cancel( 1);
    
    Thread.sleep( 1500);
    assertFalse( "Callback completed", callback.complete);
    assertFalse( "Callback succeeded", callback.success);
  }
  
  @Test
  public void disconnectDuringExecution() throws Exception
  {
    XioClient client = new XioClient( GlobalSettings.getInstance().getDefaultExecutor());
    client.connect( "localhost", port).await();

    final StringBuilder error = new StringBuilder();
    IXioCallback callback = new IXioCallback() {
      public void onComplete( IContext context)
      {
      }
      public void onSuccess( IContext context, Object[] results)
      {
      }
      public void onError( IContext context, String message)
      {
        error.append( message);
      }
    };
    
    StatefulContext context = new StatefulContext();
    IModelObject script = new XmlIO().read( "<script><sleep>1000</sleep><return>1</return></script>");
    client.execute( context, 1, new String[ 0], script, callback, 1000);
    
    client.getChannel().disconnect().await();
    
    assertTrue( "Incorrect timeout expiration.", !error.equals( "timeout"));
    assertTrue( "Wrong is empty.", error.length() > 0);
  }
  
  private void createClients( int count) throws IOException
  {
    clients = new ArrayList<XioClient>();
    for( int i=0; i<count; i++)
    {
      XioClient client = new XioClient( GlobalSettings.getInstance().getDefaultExecutor());
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
