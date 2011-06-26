package org.xmodel.net.stream;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.xmodel.compress.ICompressor;
import org.xmodel.compress.TabularCompressor;
import org.xmodel.compress.TabularCompressor.PostCompression;
import org.xmodel.external.IExternalReference;
import org.xmodel.log.Log;

/**
 * A class that represents a TCP connection.
 */
public final class Connection
{
  /**
   * Create a new connection.
   * @param manager The TcpManager instance.
   * @param channel The channel.
   * @param listener The TCP event listener.
   */
  Connection( TcpManager manager, SocketChannel channel, ITcpListener listener)
  {
    this.manager = manager;
    this.channel = channel;
    this.listener = listener;
    this.semaphore = new Semaphore( 0);
    
    buffer = ByteBuffer.allocateDirect( 4096);
    buffer.flip();
  }
  
  /**
   * @return Returns the remote host address.
   */
  public String getAddress()
  {
    return address.getAddress().getHostAddress();
  }
  
  /**
   * @return Returns the remote host port.
   */
  public int getPort()
  {
    return address.getPort();
  }
  
  /**
   * @return Returns null or the underlying channel.
   */
  public SocketChannel getChannel()
  {
    return channel;
  }

  /**
   * Called when the channel becomes connected.
   * @param channel The channel.
   */
  void connected( SocketChannel channel) throws IOException
  {
    this.channel = channel;
    this.address = (InetSocketAddress)channel.socket().getRemoteSocketAddress();
    if ( listener != null) listener.onConnect( this);
    semaphore.release();
  }
  
  /**
   * Wait for the connection to be established.
   * @param timeout The timeout in milliseconds.
   * @return Returns true if the connection was established.
   */
  boolean waitForConnect( int timeout)
  {
    try 
    { 
      return semaphore.tryAcquire( timeout, TimeUnit.MILLISECONDS);
    } 
    catch( InterruptedException e) 
    {
      return false;
    }
  }
  
  /**
   * Close this connection.
   * @param nice True if the connection was closed nicely.
   */
  public void close( boolean nice)
  {
    try 
    {
      if ( channel != null) channel.close();
    }
    catch( IOException e)
    {
    }
    channel = null;
    
    if ( listener != null) listener.onClose( this);
  }
  
  /**
   * @return Returns true if the connection is still open.
   */
  public boolean isOpen()
  {
    return channel != null;
  }

  /**
   * @return Returns the number of bytes read.
   */
  int read() throws IOException
  {
    if ( channel == null) return -1;

    // unflip
    if ( buffer.position() < buffer.limit())
    {
      buffer.position( buffer.limit());
      buffer.limit( buffer.capacity());
    }
    else
    {
      buffer.clear();
    }
    
    insureCapacity();
    
    int nread = channel.read( buffer);
    if ( nread == -1) return nread;

    //optimizeCapacity();
    
    if ( listener != null && nread > 0) 
    {
      buffer.flip();
      log.debugf( "READ\n%s\n", Util.dump( buffer));
      listener.onReceive( this, buffer);
    }
    
    return nread;
  }

  /**
   * Synchronously write the content of the specified buffer to the connection.
   * @param buffer The buffer.
   */
  public void write( ByteBuffer buffer) throws IOException
  {
    manager.write( channel, buffer);
  }

  /**
   * Insure that the internal read buffer has sufficient capacity. This will allocate or reallocate
   * the buffer as necessary to insure that the limit of the buffer does not exceed the threshold.
   */
  private void insureCapacity()
  {
    if ( buffer.position() == buffer.limit())
    {
      ByteBuffer larger = ByteBuffer.allocate( buffer.capacity() << 1);
      buffer.flip();
      larger.put( buffer);
      buffer = larger;
    }
  }

  /**
   * Close this connection.
   */
  public void close()
  {
    if ( channel != null) 
    {
      try { channel.close();} catch( Exception e) {}
      channel = null;
    }
  }

  /**
   * Note the violation of layering here ;)
   * @return Returns the external reference index associated with this connection.
   */
  public Map<String, IExternalReference> getIndex()
  {
    if ( index == null) index = new HashMap<String, IExternalReference>();
    return index;
  }

  /**
   * @return Returns the compressor for this connection.
   */
  public ICompressor getCompressor()
  {
    if ( compressor == null) compressor = new TabularCompressor( PostCompression.zip);
    return compressor;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return String.format( "%s:%d", getAddress(), getPort());
  }

  private final static Log log = Log.getLog( "org.xmodel.net.stream");

  private TcpManager manager;
  private ITcpListener listener;
  private InetSocketAddress address;
  private SocketChannel channel;
  private ByteBuffer buffer;
  
  // connection semaphore
  Semaphore semaphore;
  
  // NetworkCachingPolicy support
  private Map<String, IExternalReference> index;
  private TabularCompressor compressor;
}
