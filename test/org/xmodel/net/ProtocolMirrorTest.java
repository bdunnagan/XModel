package org.xmodel.net;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xmodel.IModelObject;
import org.xmodel.ModelObject;
import org.xmodel.xpath.expression.StatefulContext;

/**
 * Test cases for data mirroring.
 */
public class ProtocolMirrorTest
{
  public final static String host = "localhost";
  public final static int port = 10000;
  public final static int timeout = 30000;
  public final static int defaultClientCount = 10;
  
  @Before public void start() throws IOException
  {
    context = new StatefulContext();
    
    server = new XioServer( context, context);
    server.start( host, port);
    
//    Log.getLog( TcpBase.class).setLevel( Log.all);
//    Log.getLog( Client.class).setLevel( Log.all);
//    Log.getLog( Server.class).setLevel( Log.all);
//    Log.getLog( Connection.class).setLevel( Log.all);
//    Log.getLog( Protocol.class).setLevel( Log.all);
  }
  
  @After public void shutdown() throws IOException
  {
    for( XioClient client: clients)
      client.close();
    
    clients.clear();
    clients = null;
    
    server.stop();
    server = null;
  }
  
  @Test public void attachTest() throws Exception
  {
    createClients( defaultClientCount);
    
    IModelObject model = new ModelObject( "server");
    for( int i=0; i<defaultClientCount; i++)
    {
      IModelObject element = new ModelObject( "client", ""+i);
      model.addChild( element);
    }
    
    context.set( "model", model);

    for( int i=0; i<defaultClientCount; i++)
    {
//      Session session = clients.get( i);
//      String xpath = String.format( "client[ @id = %d]", i);
    }
  }
  
  private void createClients( int count) throws Exception
  {
    clients = new ArrayList<XioClient>();
    for( int i=0; i<count; i++)
    {
      XioClient client = new XioClient( null, null);
      client.connect( host, port).await( timeout);
      clients.add( client);
    }
  }
  
  private StatefulContext context;
  private XioServer server;
  private List<XioClient> clients;
}
