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

import org.xmodel.log.Log;

public class TcpClient
{
  public TcpClient( ITcpListener listener)
  {
    this.listener = listener;
    pending = new HashMap<Channel, Connection>();
    connected = new HashMap<Channel, Connection>();
  }
  
  /**
   * Connect to the specified remote address.
   * @param host The remote host.
   * @param port The remote port.
   * @param timeout The timeout in milliseconds.
   */
  public Connection connect( String host, int port, int timeout) throws IOException
  {
    address = new InetSocketAddress( host, port);
    SocketChannel channel = SocketChannel.open();
    channel.configureBlocking( false);
    
    if ( selector == null) selector = SelectorProvider.provider().openSelector();
    channel.register( selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ);
    
    channel.connect( address);
    process( timeout);
    
    Connection connection = new Connection( listener);
    pending.put( channel, connection);
    return connection;
  }
  
  /**
   * Reconnect the specified connection.
   * @param connection The connection.
   * @param timeout The timeout in milliseconds.
   */
  public void reconnect( Connection connection, int timeout) throws IOException
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
    process( timeout);
  }

  /**
   * Start the server.
   * @param listener The TCP event listener.
   */
  public void start( ITcpListener listener)
  {
    this.listener = listener;
    
    Runnable runnable = new Runnable() {
      public void run()
      {
        thread();
      }
    };
    
    thread = new Thread( runnable, "client");
    thread.setDaemon( true);
    thread.start();
  }
  
  /**
   * Process socket connect/read events.
   * @param timeout The timeout in milliseconds.
   */
  private void process( int timeout) throws IOException
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
        else if ( (readyOps & SelectionKey.OP_READ) != 0)
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
  
  /**
   * Client thread entry-point.
   */
  private void thread()
  {
    exit = false;
    while( !exit)
    {
      try
      {
        process( 500);
      }
      catch( IOException e)
      {
        log.exception( e);
        try { Thread.sleep( 500);} catch( InterruptedException i) {}
      }
    }
  }

  private final static Log log = Log.getLog( "org.xmodel.net.stream");
  
  private InetSocketAddress address;
  private Selector selector;
  private Map<Channel, Connection> pending;
  private Map<Channel, Connection> connected;
  private ITcpListener listener;
  private Thread thread;
  private boolean exit;
}
