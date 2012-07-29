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
import java.util.concurrent.atomic.AtomicInteger;
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
   * Set the SSL instance (prior to calling <code>start( boolean)</code>.
   * @param ssl An SSL instance.
   */
  protected synchronized void useSSL( SSL ssl)
  {
    if ( thread != null) throw new IllegalStateException();
    this.ssl = ssl;
  }
  
  /**
   * Start the socket servicing thread.
   * @param daemon True if servicing thread should be a daemon.
   */
  public synchronized void start( boolean daemon) throws IOException
  {
    Runnable runnable = new Runnable() {
      public void run()
      {
        thread();
      }
    };
    
    String name = String.format( "%s-%d", getClass().getSimpleName(), counter.incrementAndGet());
    thread = new Thread( runnable, name);
    thread.setDaemon( daemon);
    thread.start();
  }
  
  /**
   * Stop the socket servicing thread.
   */
  public synchronized void stop()
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
   * @return Returns an established connection.
   */
  protected Connection connect( String host, int port, int timeout, ILink.IListener listener) throws IOException
  {
    log.debugf( "Opening a connection to %s:%d, timeout=%d ...", host, port, timeout);
    
    SocketChannel channel = SocketChannel.open();
    channel.configureBlocking( false);
    channel.socket().setKeepAlive( true);
    channel.socket().setTrafficClass( 0x04);
    
    channel.connect( new InetSocketAddress( host, port));
    Connection connection = createConnection( channel, listener);
    
    Request request = new Request();
    request.channel = channel;
    request.buffer = null;
    request.ops = SelectionKey.OP_CONNECT;
    
    enqueue( request);
    
    try
    {
      if ( !connection.waitForConnect( timeout)) 
      {
        log.debug( "Timeout expired!");
        return null;
      }
    }
    catch( InterruptedException e)
    {
      Thread.interrupted();
      log.debug( "Thread interrupted.");
      return null;
    }
    
    log.debug( "Connection established.");
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
    
    log.debugf( "Reconnecting to %s:%d, timeout=%d ...", connection.getAddress(), connection.getPort(), timeout);
    
    Request request = new Request();
    request.channel = connection.getChannel();
    request.buffer = null;
    request.ops = SelectionKey.OP_CONNECT;
    
    enqueue( request);
  
    try
    {
      if ( !connection.waitForConnect( timeout))
      {
        log.debug( "Timeout expired!");
        return false;
      }
    }
    catch( InterruptedException e)
    {
      log.debugf( "Thread interrupted!");
      Thread.interrupted();
      return false;
    }
    
    log.debug( "Connection re-established.");
    return true;
  }

  /**
   * Enqueue a request to write data.
   * @param channel The channel.
   * @param bytes The bytes to be written.
   */
  void write( SocketChannel channel, byte[] bytes)
  {
    ByteBuffer clone = ByteBuffer.allocate( bytes.length);
    clone.put( bytes);
    clone.flip();
    
    Request request = new Request();
    request.channel = channel;
    request.buffer = clone;
    request.ops = SelectionKey.OP_READ | SelectionKey.OP_WRITE;
    
    enqueue( request);
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
    
    InetSocketAddress local = (InetSocketAddress)connection.getChannel().socket().getLocalSocketAddress();
    log.debugf( "Accepted incoming connection from %s:%d.", local.getAddress().getHostAddress(), local.getPort());
  }
  
  /**
   * Read from a ready channel.
   * @param key The selection key.
   */
  protected void read( SelectionKey key) throws IOException
  {
    SocketChannel channel = (SocketChannel)key.channel();
    Connection connection = connections.get( channel);
    try
    {
      connection.buffer = prepareReadBuffer( connection.buffer);

      // read (optionally use ssl)
      int nread = 0;
      if ( ssl != null)
      {
        nread = ssl.read( channel, connection);
      }
      else
      {
        nread = channel.read( connection.buffer);
        //log.debugf( ">>  %s", Util.dump( buffer, ""));
      }
      
      if ( nread > 0)
      {
        connection.buffer.flip();
        connection.notifyRead( connection.buffer);
      }
      else if ( nread == -1)
      {
        close( key);
      }
    }
    catch( Exception e)
    {
      log.exception( e);
      close( key);
    }
  }

  /**
   * Prepare the specified read buffer for appending new data.
   * @param buffer The read buffer.
   * @return Returns a larger buffer if necessary.
   */
  private ByteBuffer prepareReadBuffer( ByteBuffer buffer)
  {
    if ( buffer.position() < buffer.limit())
    {
      buffer.position( buffer.limit());
      buffer.limit( buffer.capacity());
    }
    else
    {
      buffer.clear();
    }
    
    if ( buffer.position() == buffer.capacity())
    {
      ByteBuffer larger = ByteBuffer.allocate( buffer.capacity() << 1);
      buffer.flip();
      larger.put( buffer);
      return larger;
    }
    
    return buffer;
  }
  
  /**
   * Write to a ready channel.
   * @param key The selection key.
   */
  protected void write( SelectionKey key) throws IOException
  {
    Request request = pending.get( key.channel());
    if ( request != null)
    {
      //log.debugf( "WRITE\n%s\n", Util.dump( request.buffer));
      //int n = request.buffer.remaining();
      //int wrote = request.channel.write( request.buffer);
      //log.infof( "wrote: %d - %d = %d", n, wrote, request.buffer.remaining());
      
      if ( ssl != null)
      {
        ssl.write( request.channel, request.buffer);
      }
      else
      {
        request.channel.write( request.buffer);
      }
      
      if ( request.buffer.remaining() == 0)
      {
        pending.remove( key.channel());
      
        // disable write events if queue is empty or next request is for a different channel
        request = queue.peek();
        if ( request == null || request.channel != key.channel())
        {
          key.channel().register( selector, SelectionKey.OP_READ);
        }
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
        if ( readyKey.isReadable()) read( readyKey);
        if ( readyKey.isWritable()) write( readyKey);
        if ( readyKey.isAcceptable()) accept( readyKey);
        if ( readyKey.isConnectable()) connect( readyKey);
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
        Request request = queue.peek();
        if ( request != null) 
        {
          if ( !pending.containsKey( request.channel))
          {
            queue.remove();
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
  
  protected final static Log log = Log.getLog( TcpBase.class);
  private static AtomicInteger counter = new AtomicInteger();

  private ILink.IListener listener;
  protected Selector selector;
  private Map<Channel, Connection> connections;
  private Queue<Request> queue;
  private Map<Channel, Request> pending;
  private SSL ssl;
  private Thread thread;
  private boolean exit;
}
