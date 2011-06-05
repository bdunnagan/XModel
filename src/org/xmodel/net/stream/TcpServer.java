package org.xmodel.net.stream;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xmodel.log.Log;

/**
 * A java.nio based Server.
 */
public final class TcpServer
{
  /**
   * Create a server bound to the specified host and port.
   * @param host The host address.
   * @param port The port.
   */
  public TcpServer( String host, int port) throws IOException
  {
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
    
    thread = new Thread( runnable, "server");
    thread.setDaemon( true);
    thread.start();
  }
  
  /**
   * Process socket accept/read events.
   * @param timeout The timeout in milliseconds.
   */
  private void process( int timeout) throws IOException
  {
    // wait for connection
    if ( selector.select( timeout) == 0) return; 
      
    // handle events
    Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
    while( iter.hasNext())
    {
      SelectionKey readyKey = iter.next();
      iter.remove();
      
      if ( !readyKey.isValid()) continue;
      
      try
      {
        if ( readyKey.isAcceptable())
        {
          accept( readyKey);
        }
        else
        {
          read( readyKey);
        }
      }
      catch( IOException e)
      {
        log.exception( e);
      }
    }
  }
  
  /**
   * Accept an incoming connection.
   * @param key The selection key.
   */
  private void accept( SelectionKey key) throws IOException
  {
    SocketChannel newChannel = serverChannel.accept();
    newChannel.configureBlocking( false);
    newChannel.register( selector, SelectionKey.OP_READ);
    
    Connection connection = new Connection( listener);
    connection.connected( newChannel);
    connections.put( newChannel, connection);
  }
  
  /**
   * Read from a ready channel.
   * @param key The selection key.
   */
  private void read( SelectionKey key) throws IOException
  {
    SocketChannel socketChannel = (SocketChannel)key.channel();
    Connection connection = connections.get( socketChannel);
    try
    {
      int nread = connection.read();
      if ( nread == -1) close( key);
    }
    catch( Exception e)
    {
      close( key);
    }
  }
  
  /**
   * Close the connection with the specified key.
   * @param key The selected key.
   */
  private void close( SelectionKey key)
  {
    SocketChannel channel = (SocketChannel)key.channel();
    Connection connection = connections.get( channel);
    connections.remove( channel);
    connection.close( false);
    key.cancel();
  }
  
  /**
   * Close the server socket.
   */
  public void close()
  {
    try
    {
      if ( serverChannel != null) serverChannel.close();
    }
    catch( IOException e)
    {
      log.exception( e);
    }
    serverChannel = null;
  }
  
  /**
   * Server thread entry-point.
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
  
  private InetSocketAddress localAddress;
  private ServerSocketChannel serverChannel;
  private Selector selector;
  private Map<Channel, Connection> connections;
  private ITcpListener listener;
  private Thread thread;
  private boolean exit;
}
