package org.xmodel.net.stream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.xmodel.log.Log;

/**
 * A base class for java.nio based clients and servers.
 */
public abstract class TcpManager
{
  public TcpManager( ITcpListener listener) throws IOException
  {
    this.listener = listener;
    connections = new HashMap<Channel, Connection>();
    selector = SelectorProvider.provider().openSelector();
    writeQueue = new ArrayBlockingQueue<Request>( 1);
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
    Request request = new Request();
    request.channel = channel;
    request.buffer = buffer;
    try { writeQueue.put( request);} catch( InterruptedException e) {}
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
   * @return Returns the new Connection.
   */
  protected Connection createConnection( SocketChannel channel, int ops) throws IOException
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
      System.out.printf( "WRITE\n%s\n", Util.dump( request.buffer));
      request.channel.write( request.buffer);
      request.channel.register( selector, SelectionKey.OP_READ);
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
  private ITcpListener listener;
  private Map<Channel, Connection> connections;
  private BlockingQueue<Request> writeQueue;
  private Thread thread;
  private boolean exit;
}
