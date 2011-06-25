package org.xmodel.net.stream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.xmodel.log.Log;

/**
 * A base class for java.nio based clients and servers.
 */
public abstract class TcpManager
{
  public TcpManager() throws IOException
  {
    connections = Collections.synchronizedMap( new HashMap<Channel, Connection>());
    selector = SelectorProvider.provider().openSelector();
    writeQueue = new ConcurrentLinkedQueue<Request>();
  }

  /**
   * Start the socket servicing thread.
   */
  public void start()
  {
    Runnable runnable = new Runnable() {
      public void run()
      {
        thread();
      }
    };
    
    thread = new Thread( runnable, getClass().getSimpleName());
    thread.setDaemon( true);
    thread.start();
  }
  
  /**
   * Stop the socket servicing thread.
   */
  public void stop()
  {
    exit = true;
    selector.wakeup();
  }
  
  /**
   * Enqueue a request to write data.
   * @param channel The channel.
   * @param buffer The buffer to be written.
   */
  void write( SocketChannel channel, ByteBuffer buffer)
  {
    // TODO: this could be allocated direct if it lived on the Connection
    ByteBuffer clone = ByteBuffer.allocate( buffer.remaining());
    clone.put( buffer);
    clone.flip();
    
    Request request = new Request();
    request.channel = channel;
    request.buffer = clone;
    
    writeQueue.offer( request);
    selector.wakeup();
  }
  
  /**
   * Process socket events.
   * @param timeout The timeout in milliseconds.
   * @return Returns false if a timeout occurs.
   */
  protected abstract boolean process( int timeout) throws IOException;
  
  /**
   * Create a new Connection instance for the specified channel.
   * @param channel The channel.
   * @param ops The selector operations to register on the channel.
   * @parma listener The listener for socket events.
   * @return Returns the new Connection.
   */
  protected Connection createConnection( SocketChannel channel, int ops, ITcpListener listener) throws IOException
  {
    // configure channel
    channel.configureBlocking( false);
    channel.register( selector, ops);
    
    // create connection
    Connection connection = new Connection( this, listener);
    connection.connected( channel);
    connections.put( channel, connection);
    
    return connection;
  }
  
  /**
   * Read from a ready channel.
   * @param key The selection key.
   */
  protected void read( SelectionKey key) throws IOException
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
   * Write to a ready channel.
   * @param key The selection key.
   */
  protected void write( SelectionKey key) throws IOException
  {
    Request request = writeQueue.poll();
    if ( request != null)
    {
      //log.debugf( "WRITE (%d)\n%s\n", writeQueue.size(), Util.dump( request.buffer));
      request.channel.write( request.buffer);
      
      // disable write events if queue is empty or next request is for a different channel
      request = writeQueue.peek();
      if ( request == null || request.channel != key.channel())
      {
        key.channel().register( selector, SelectionKey.OP_READ);
      }
    }
  }
  
  /**
   * Close the connection with the specified key.
   * @param key The selected key.
   */
  private void close( SelectionKey key)
  {
    SocketChannel channel = (SocketChannel)key.channel();
    Connection connection = connections.remove( channel);
    if ( connection != null) connection.close( false);
    key.cancel();
  }
  
  private void thread()
  {
    exit = false;
    while( !exit)
    {
      try
      {
        // process socket events
        process( 0);
        
        // turn on write operations if queue is not empty
        Request request = writeQueue.peek();
        if ( request != null) 
        {
          request.channel.register( selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        }
      }
      catch( IOException e)
      {
        log.exception( e);
        break;
      }
    }
  }

  protected static class Request
  {
    public SocketChannel channel;
    public ByteBuffer buffer;
  }
  
  private final static Log log = Log.getLog( "org.xmodel.net.stream");
  
  protected Selector selector;
  private Map<Channel, Connection> connections;
  private Queue<Request> writeQueue;
  private Thread thread;
  private boolean exit;
}
