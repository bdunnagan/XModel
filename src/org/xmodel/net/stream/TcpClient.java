package org.xmodel.net.stream;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.xmodel.net.INetFramer;
import org.xmodel.net.INetReceiver;

public class TcpClient implements ITcpAgent
{
  public TcpClient( INetFramer framer, INetReceiver receiver)
  {
    this.framer = framer;
    this.receiver = receiver;
    pending = new HashMap<Channel, Connection>();
    connected = new HashMap<Channel, Connection>();
  }
  
  /**
   * Connect to the specified remote address.
   * @param host The remote host.
   * @param port The remote port.
   */
  public Connection connect( String host, int port) throws IOException
  {
    address = new InetSocketAddress( host, port);
    SocketChannel channel = SocketChannel.open();
    channel.configureBlocking( false);
    
    if ( selector == null) selector = SelectorProvider.provider().openSelector();
    channel.register( selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ);
    
    channel.connect( address);
    
    Connection connection = new Connection( this, framer, receiver);
    pending.put( channel, connection);
    return connection;
  }
  
  /**
   * Reconnect the specified connection.
   * @param connection The connection.
   */
  public void reconnect( Connection connection) throws IOException
  {
    SocketChannel channel = connection.getChannel();
    if ( channel != null)
    {
      try { channel.close();} catch( Exception e) {}
    }
    
    pending.remove( channel);
    connected.remove( channel);
    
    address = new InetSocketAddress( connection.getAddress(), connection.getPort());
    channel = SocketChannel.open();
    channel.configureBlocking( false);
    
    if ( selector == null) selector = SelectorProvider.provider().openSelector();
    channel.register( selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ);
    
    pending.put( channel, connection);
    channel.connect( address);
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.nu.stream.ITcpPeer#process()
   */
  public void process() throws IOException
  {
    process( 0);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.nu.stream.ITcpPeer#process(int)
   */
  public void process( int timeout) throws IOException
  {
    if ( selector.select( timeout) == 0) return; 

    Set<SelectionKey> readyKeys = selector.selectedKeys();
    for( SelectionKey readyKey: readyKeys)
    {
      SocketChannel channel = (SocketChannel)readyKey.channel();
      try
      {
        int readyOps = readyKey.readyOps();
        if ( (readyOps & SelectionKey.OP_CONNECT) != 0)
        {
          Connection connection = pending.remove( channel);
          try
          {
            channel.finishConnect();
            channel.register( selector, SelectionKey.OP_READ);
            connected.put( channel, connection);
            connection.connected( channel);
          }
          catch( IOException e)
          {
            connection.close( false);
          }
        }
        else
        {
          Connection connection = connected.get( channel);
          try
          {
            int nread = connection.read();
            while( nread > 0) nread = connection.read();
          }
          catch( IOException e)
          {
            connection.close( false);
          }
        }
      }
      catch( CancelledKeyException e)
      {
        Connection connection = connected.get( address);
        connection.close( false);
      }
    }
    
    readyKeys.clear();
  }
  
  private INetFramer framer;
  private INetReceiver receiver;
  private InetSocketAddress address;
  private Selector selector;
  private Map<Channel, Connection> pending;
  private Map<Channel, Connection> connected;
}
