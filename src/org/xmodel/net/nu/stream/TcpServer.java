package org.xmodel.net.nu.stream;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.xmodel.net.nu.INetFramer;

/**
 * A java.nio based Server. This class provides a minimal non-blocking server.
 */
public class TcpServer implements ITcpAgent
{
  /**
   * Create a server bound to the specified host and port.
   * @param host The host address.
   * @param port The port.
   * @param framer The message framer.
   * @param receiver The recipient.
   */
  public TcpServer( String host, int port, INetFramer framer, IReceiver receiver) throws IOException
  {
    this.framer = framer;
    this.receiver = receiver;
    
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

  /* (non-Javadoc)
   * @see org.xmodel.net.nu.stream.ITcpPeer#process()
   */
  @Override
  public void process() throws IOException
  {
    process( 0);
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.nu.stream.ITcpPeer#process(int)
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
          
          Connection connection = new Connection( this, framer, receiver);
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
  private INetFramer framer;
  private IReceiver receiver;
}
