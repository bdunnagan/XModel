package org.xmodel.net;

import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.xmodel.net.ILink.IListener;
import org.xmodel.net.stream.Connection;
import org.xmodel.net.stream.SSL;
import org.xmodel.net.stream.SSL.Authentication;
import org.xmodel.net.stream.TcpClient;
import org.xmodel.net.stream.TcpServer;

/**
 * Test cases for SSL implementation.
 */
public class SSLTest
{
  public final static String host = "localhost";
  public final static int port = 10000;
  
  @Test public void connectAndDisconnect() throws GeneralSecurityException, IOException, InterruptedException
  {
    final String clientMessage = "Hello!";
    final SynchronousQueue<String> queue = new SynchronousQueue<String>();
    
    TcpServer server = new TcpServer( host, port, new IListener() {
      public void onReceive( ILink link, ByteBuffer buffer)
      {
        byte[] bytes = new byte[ buffer.remaining()];
        buffer.get( bytes);
        queue.offer( new String( bytes));
      }
      public void onClose( ILink link)
      {
      }
    });

    TcpClient client = new TcpClient();
    
    SSL serverSSL = new SSL( getClass().getResourceAsStream( "keystore.jks"), "test00", getClass().getResourceAsStream( "keystore.jks"), "test00", Authentication.none);
    SSL clientSSL = new SSL( getClass().getResourceAsStream( "keystore.jks"), "test00", getClass().getResourceAsStream( "keystore.jks"), "test00", Authentication.none);
    server.useSSL( serverSSL);
    client.useSSL( clientSSL);
    server.start( false);
    client.start( false);
    
    Connection connection = client.connect( host, port, 240000, new IListener() {
      public void onReceive( ILink link, ByteBuffer buffer)
      {
      }
      public void onClose( ILink link)
      {
      }
    });
    
    assertTrue( "Connection not established", connection != null);
    
    connection.send( clientMessage.getBytes());
    
    String message = queue.poll( 100, TimeUnit.SECONDS);
    assertTrue( "Client message not received.", clientMessage != null);
    if ( clientMessage != null)
      assertTrue( "Incorrect message received", clientMessage.equals( message));
  }
}
