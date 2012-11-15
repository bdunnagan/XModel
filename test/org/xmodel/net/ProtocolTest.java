package org.xmodel.net;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xmodel.IModelObject;
import org.xmodel.ModelObject;
import org.xmodel.xml.XmlException;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.expression.StatefulContext;

/**
 * Test case for many clients accessing a single server.
 */
public class ProtocolTest
{
  public final static String address = "localhost";
  public final static int port = 10000;
  public final static int timeout = 30000;
  public final static int defaultClientCount = 10;
  public final static int defaultConnectionPasses = 100;
  public final static int largePayloadCount = 100000;
  public final static int smallPayloadCount = 100;
  public final static int executeLoopCount = 100;
  
  @Before public void start() throws IOException
  {
    server = new Server( null, null);
    server.start( address, port);
    
//    Log.getLog( TcpBase.class).setLevel( Log.all);
//    Log.getLog( Connection.class).setLevel( Log.all);
    //Log.getLog( Client.class).setLevel( Log.all);
    //Log.getLog( Server.class).setLevel( Log.all);
    //Log.getLog( Protocol.class).setLevel( Log.all);
  }
  
  @After public void shutdown() throws IOException
  {
    for( Client client: clients)
    {
      if ( client.isConnected()) client.close();
      client = null;
    }
    clients.clear();
    clients = null;
    
    server.stop();
    server = null;
  }
  
  @Test public void connectAndDisconnect() throws Exception
  {
    createClients( defaultClientCount);
    
    for( Client client: clients)
    {
      client.connect( address, port).await();
      assertTrue( "Client is not connected", client.isConnected());
    }
    
    Thread.sleep( 2000);
    
    for( Client client: clients)
    {
      client.close().await();
      assertFalse( "Client is not disconnected", client.isConnected());
    }
    
    for( Client client: clients)
    {
      client.connect( address, port).await();
      assertTrue( "Client is not connected", client.isConnected());
      
      client.close().await();
      assertFalse( "Client is not disconnected", client.isConnected());
    }
    
    int passes = defaultConnectionPasses;
    int tally = 0;
    for( int i=0; i<passes; i++)
    {
      for( Client client: clients)
      {
        client.connect( address, port).await();
        client.close();
        if ( !client.isConnected())
        {
          tally++;
        }
      }
    }
    assertTrue( "Client connect/disconnect tally is not correct", tally == clients.size() * passes);
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
    
    Client client = clients.get( 0);
    client.connect( address, port).await();
    
    IModelObject script = new XmlIO().read( scriptXml);
    Object[] result = client.execute( context, new String[] { "payload"}, script, timeout);
    
    int count = ((Number)result[ 0]).intValue();
    assertTrue( String.format( "Payload count is %d, should be %d", count, largePayloadCount), count == largePayloadCount);
  }
  
  @Test public void concurrentExecution() throws IOException, XmlException, InterruptedException
  {
    createClients( defaultClientCount);
    
    String scriptXml = "" +
        "<script>" +
        "  <return>count( $payload/*)</return>" +
        "</script>";
    
    IModelObject payload = new ModelObject( "payload");
    for( int j=0; j<smallPayloadCount; j++)
    {
      IModelObject child = new ModelObject( "payload");
      payload.addChild( child);
    }
  
    StatefulContext context = new StatefulContext();
    context.set( "payload", payload);

    IModelObject script = new XmlIO().read( scriptXml);
    
    List<ExecuteTask> tasks = new ArrayList<ExecuteTask>();
    for( Client client: clients)
    {
      client.connect( address, port).await();
      tasks.add( new ExecuteTask( client, context, script));
    }
    
    ExecutorService executor = Executors.newFixedThreadPool( defaultClientCount);
    for( ExecuteTask task: tasks)
      executor.execute( task);
    
    executor.shutdown();
    executor.awaitTermination( timeout, TimeUnit.MILLISECONDS);
    
    for( ExecuteTask task: tasks)
    {
      assertTrue( String.format( "Caught: %s", task.e), task.e == null);
      
      for( Object[] result: task.results)
      {
        int count = ((Number)result[ 0]).intValue();
        assertTrue( String.format( "Payload count is %d, should be %d", count, smallPayloadCount), count == smallPayloadCount);
      }
    }
  }
  
  private void createClients( int count) throws IOException
  {
    clients = new ArrayList<Client>();
    for( int i=0; i<count; i++)
    {
      Client client = new Client();
      clients.add( client);
    }
  }
  
  private static class ExecuteTask implements Runnable
  {
    public ExecuteTask( Client client, StatefulContext context, IModelObject script)
    {
      this.client = client;
      this.context = context;
      this.script = script;
      this.results = new ArrayList<Object[]>();
    }
    
    public void run()
    {
      try
      {
        for( int i=0; i<executeLoopCount; i++)
        {
          Object[] result = client.execute( context, new String[] { "payload"}, script, timeout);
          results.add( result);
        }
      }
      catch( Exception e)
      {
        this.e = e;
      }
    }
    
    private Client client;
    private StatefulContext context;
    private IModelObject script;
    public List<Object[]> results;
    public Exception e;
  }
  
  private Server server;
  private List<Client> clients;
}
