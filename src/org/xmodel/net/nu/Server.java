package org.xmodel.net.nu;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A java.nio based Server. This class provides a minimal non-blocking server.
 */
public class Server
{
  /**
   * Create a server bound to the specified host and port.
   * @param host The host address.
   * @param port The port.
   * @param recipient The recipient.
   */
  public Server( String host, int port, IRecipient recipient) throws IOException
  {
    this.recipient = recipient;
    localAddress = new InetSocketAddress( host, port);
    connections = new HashMap<Channel, Connection>();
    
    // open
    serverChannel = ServerSocketChannel.open();
    serverChannel.configureBlocking( false);
    serverChannel.socket().bind( localAddress);
    
    // register accept selector
    selector = SelectorProvider.provider().openSelector();
    serverChannel.register( selector, SelectionKey.OP_ACCEPT);
  }

  /**
   * Process the next events from the socket.
   * @param timeout The amount of time to wait for events.
   */
  public void process( int timeout) throws IOException
  {
    // wait for connection
    if ( selector.select( timeout) == 0) return; 
      
    // handle events
    Set<SelectionKey> readyKeys = selector.selectedKeys();
    for( SelectionKey readyKey: readyKeys)
    {
      Channel channel = readyKey.channel();
      if ( serverChannel == channel)
      {
        try
        {
          SocketChannel newChannel = serverChannel.accept();
          newChannel.configureBlocking( false);
          newChannel.register( selector, SelectionKey.OP_READ);
          
          Connection connection = new Connection( recipient);
          connection.connected( newChannel);
          connections.put( newChannel, connection);
        }
        catch( IOException e)
        {
        }
      }
      else
      {
        SocketChannel socketChannel = (SocketChannel)channel;
        Connection connection = connections.get( socketChannel);
        try
        {
          int nread = connection.read();
          if ( nread < 0)
          {
            connection.close( nread == 0);
            connections.remove( channel);
            channel.close();
            continue;
          }
        }
        catch( Exception e)
        {
          connections.remove( channel);
          connection.close( false);
        }
      }
    }
    
    readyKeys.clear();
  }
  
  public void close()
  {
    try
    {
      if ( serverChannel != null) serverChannel.close();
    }
    catch( IOException e)
    {
      System.err.println( "Error closing server socket: "+e.getMessage());
    }
    
    serverChannel = null;
  }
  
  private InetSocketAddress localAddress;
  private ServerSocketChannel serverChannel;
  private Selector selector;
  private Map<Channel, Connection> connections;
  private IRecipient recipient;
  
  public static void main( String[] args) throws Exception
  {
    String[] split = args[ 0].split( "[:]");
    
    IRecipient recipient = new IRecipient() {
      public void connected( Connection connection)
      {
        System.out.println( "Connected.");
      }
      public void disconnected( Connection connection, boolean nice)
      {
        System.out.println( "Disconnected: "+nice);
      }
      public int received( Connection connection, ByteBuffer buffer)
      {
        buffer.flip();
        return 0;
      }
    };
    
    Server server = new Server( split[ 0], Integer.parseInt( split[ 1]), recipient);
    while( true)
    {
      System.out.printf( "%s\n", new Date());
      server.process( 10000);
    }
  }
}
