package org.xmodel.net.stream;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.xmodel.log.Log;
import org.xmodel.net.ILink;

/**
 * A base class for java.nio based clients and servers.
 */
public abstract class TcpBase
{
  public TcpBase() throws IOException
  {
    this( null);
  }
  
  public TcpBase( ILink.IListener listener) throws IOException
  {
    this.listener = listener;
    
    connections = Collections.synchronizedMap( new HashMap<Channel, Connection>());
    selector = SelectorProvider.provider().openSelector();
    queue = new ConcurrentLinkedQueue<Request>();
    pending = new HashMap<Channel, Request>();
  }

  /**
   * Start the socket servicing thread.
   * @param daemon True if servicing thread should be a daemon.
   */
  public void start( boolean daemon)
  {
    Runnable runnable = new Runnable() {
      public void run()
      {
        thread();
      }
    };
    
    thread = new Thread( runnable, getClass().getSimpleName());
    thread.setDaemon( daemon);
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
   * Return the connection associated with the specified channel.
   * @param channel The channel.
   * @return Return the connection associated with the specified channel.
   */
  protected Connection getConnection( Channel channel)
  {
    return connections.get( channel);
  }
  
  /**
   * Connect to the specified remote address.
   * @param host The remote host.
   * @param port The remote port.
   * @param timeout The timeout in milliseconds.
   * @param listener The listener for socket events.
   */
  protected Connection connect( String host, int port, int timeout, ILink.IListener listener) throws IOException
  {
    SocketChannel channel = SocketChannel.open();
    channel.configureBlocking( false);
    
    channel.connect( new InetSocketAddress( host, port));
    Connection connection = createConnection( channel, listener);
    
    Request request = new Request();
    request.channel = connection.getChannel();
    request.buffer = null;
    request.ops = SelectionKey.OP_CONNECT;
    
    enqueue( request);
    
    if ( !connection.waitForConnect( timeout)) return null; 
    
    return connection;
  }
  
  /**
   * Reconnect the specified connection.
   * @param connection The connection.
   * @param timeout The timeout in milliseconds.
   * @return Returns true if the connection was reestablished.
   */
  protected boolean reconnect( Connection connection, int timeout) throws IOException
  {
    if ( connection.isOpen()) connection.close();
    
    Request request = new Request();
    request.channel = connection.getChannel();
    request.buffer = null;
    request.ops = SelectionKey.OP_CONNECT;
    
    enqueue( request);
    
    return connection.waitForConnect( timeout);
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
    request.ops = SelectionKey.OP_READ | SelectionKey.OP_WRITE;
    
    enqueue( request);
  }
  
  /**
   * Enqueue the specified request and wakeup the selector.
   * @param request The request.
   */
  private void enqueue( Request request)
  {
    queue.offer( request);
    selector.wakeup();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.stream.TcpManager#process(int)
   */
  protected boolean process( int timeout) throws IOException
  {
    // wait for connection
    if ( selector.select( timeout) == 0) return false; 
      
    // handle events
    Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
    while( iter.hasNext())
    {
      SelectionKey readyKey = iter.next();
      iter.remove();
      
      if ( !readyKey.isValid()) continue;
      
      try
      {
        if ( readyKey.isReadable())
        {
          read( readyKey);
        }
        
        if ( readyKey.isWritable())
        {
          write( readyKey);
        }
        
        if ( readyKey.isAcceptable())
        {
          accept( readyKey);
        }
        
        if ( readyKey.isConnectable())
        {
          connect( readyKey);
        }
      }
      catch( IOException e)
      {
        log.verbose( e.getMessage());
      }
      catch( CancelledKeyException e)
      {
        log.verbose( e.getMessage());
      }
    }
    
    return true;
  }
    
  /**
   * Create a new Connection instance for the specified channel.
   * @param channel The channel.
   * @param listener The listener for socket events.
   * @return Returns the new Connection.
   */
  protected Connection createConnection( SocketChannel channel, ILink.IListener listener) throws IOException
  {
    Connection connection = new Connection( this, channel, listener);
    connections.put( connection.getChannel(), connection);
    return connection;
  }
  
  /**
   * Process a newly connected socket.
   * @param key The selection key.
   */
  private void connect( SelectionKey key) throws IOException
  {
    Request request = pending.remove( key.channel());
    if ( request != null)
    {
      SocketChannel channel = (SocketChannel)key.channel();
      Connection connection = getConnection( channel);
      try
      {
        channel.finishConnect();
        channel.register( selector, SelectionKey.OP_READ);
        connection.connected( channel);
      }
      catch( IOException e)
      {
        connection.close( false);
      }
    }
  }
  
  /**
   * Accept an incoming connection.
   * @param key The selection key.
   */
  private void accept( SelectionKey key) throws IOException
  {
    ServerSocketChannel channel = (ServerSocketChannel)key.channel();    
    Connection connection = createConnection( channel.accept(), listener);
    connection.getChannel().configureBlocking( false);
    connection.getChannel().register( selector, SelectionKey.OP_READ);
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
    Request request = pending.remove( key.channel());
    if ( request != null)
    {
      //log.debugf( "WRITE\n%s\n", Util.dump( request.buffer));
      request.channel.write( request.buffer);
      
      // disable write events if queue is empty or next request is for a different channel
      request = queue.peek();
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
        
        // handle requests
        Request request = queue.poll();
        if ( request != null) 
        {
          try
          {
            pending.put( request.channel, request);
            if ( request.channel != null)
            {
              request.channel.register( selector, request.ops);
            }
          }
          catch( CancelledKeyException e)
          {
            pending.remove( request.channel);
            log.exception( e);
          }
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
    public int ops;
  }
  
  private final static Log log = Log.getLog( "org.xmodel.net.stream");

  private ILink.IListener listener;
  protected Selector selector;
  private Map<Channel, Connection> connections;
  private Queue<Request> queue;
  private Map<Channel, Request> pending;
  private Thread thread;
  private boolean exit;
}
