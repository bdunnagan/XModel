package org.xmodel.net.stream;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * A class that represents a TCP connection.
 */
public final class Connection
{
  /**
   * Create a new connection.
   * @param tcp The TcpManager instance.
   * @param channel The channel.
   * @param listener The TCP event listener.
   */
  Connection( TcpBase tcp, SocketChannel channel, ITcpListener listener)
  {
    this.tcp = tcp;
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
      semaphore.release();
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
      //log.debugf( "READ\n%s\n", Util.dump( buffer));
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
    tcp.write( channel, buffer);
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

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return String.format( "%s:%d", getAddress(), getPort());
  }

  //private final static Log log = Log.getLog( "org.xmodel.net.stream");

  private TcpBase tcp;
  private ITcpListener listener;
  private InetSocketAddress address;
  private SocketChannel channel;
  private ByteBuffer buffer;
  
  // connection semaphore
  Semaphore semaphore;
}
