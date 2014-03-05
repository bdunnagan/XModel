package org.xmodel.net;

import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xmodel.GlobalSettings;
import org.xmodel.IModelObject;
import org.xmodel.log.Log;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.expression.StatefulContext;

public class RegistryTest
{

  @Before
  public void setUp() throws Exception
  {
  }

  @After
  public void tearDown() throws Exception
  {
  }

  @Test
  public void register() throws Exception
  {
    Log.setLevel( "org.xmodel.net.*", Log.problems | Log.debug);
    
    StatefulContext serverContext = new StatefulContext();
    serverContext.set( "name", "Server");
    XioServer server = new XioServer( serverContext);
    server.start( "localhost", 10000);

    StatefulContext clientContext = new StatefulContext();
    clientContext.set( "name", "Client");
    NettyXioClient client = new NettyXioClient( clientContext, GlobalSettings.getInstance().getScheduler(), Executors.newCachedThreadPool());
    client.connect( "localhost", 10000).await();
    
    final Semaphore s4 = new Semaphore( 0);
    server.getPeerRegistry().addListener( new IXioPeerRegistryListener() {
      public void onRegister( XioPeer peer, String name)
      {
        s4.release();
      }
      public void onUnregister( XioPeer peer, String name)
      {
        s4.release();
      }
    });
    
    client.register( "Test");
    s4.acquire();
    Iterator<XioPeer> iterator = server.getPeerRegistry().lookupByName( "Test");
    assertTrue( "Peer lookup failed.", iterator.next() != null);

    client.unregister( "Test");
    s4.acquire();
    iterator = server.getPeerRegistry().lookupByName( "Test");
    assertTrue( "Peer not unregistered.", !iterator.hasNext());
    
    client.register( "Test");
    s4.acquire();
    iterator = server.getPeerRegistry().lookupByName( "Test");
    XioPeer peer = iterator.next();

    IModelObject script = new XmlIO().read( "<script><return>$name</return></script>");
    Object[] result = peer.execute( serverContext, new String[ 0], script, 1000);
    assertTrue( "Wrong value for $name.", result[ 0].equals( "Client"));
    
    client.close().await();
    client.connect( "localhost", 10000);
    result = peer.execute( serverContext, new String[ 0], script, 5000);
    assertTrue( "Wrong value for $name.", result[ 0].equals( "Client"));
    
    server.stop();
  }
}
