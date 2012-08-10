package org.xmodel.net;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xmodel.IModelObject;
import org.xmodel.ImmediateDispatcher;
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
    
    server = new Server( host, port);
    server.setPingTimeout( timeout);
    server.setServerContext( context);
    server.setDispatcher( new ImmediateDispatcher());
    server.start( false);
    
//    Log.getLog( TcpBase.class).setLevel( Log.all);
//    Log.getLog( Client.class).setLevel( Log.all);
//    Log.getLog( Server.class).setLevel( Log.all);
//    Log.getLog( Connection.class).setLevel( Log.all);
//    Log.getLog( Protocol.class).setLevel( Log.all);
  }
  
  @After public void shutdown() throws IOException
  {
    for( Session session: clients)
      session.close();
    
    clients.clear();
    clients = null;
    
    server.stop();
    server = null;
  }
  
  @Test public void attachTest() throws IOException
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
      Session session = clients.get( i);
      String xpath = String.format( "client[ @id = %d]", i);
    }
  }
  
  private void createClients( int count) throws IOException
  {
    clients = new ArrayList<Session>();
    for( int i=0; i<count; i++)
    {
      Client client = new Client( host, port, true);
      client.setPingTimeout( timeout);
      Session session = client.connect( timeout);
      clients.add( session);
    }
  }
  
  private StatefulContext context;
  private Server server;
  private List<Session> clients;
}
