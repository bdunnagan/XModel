package org.xmodel.net.stream;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

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
   * @param listener The TCP event listener.
   */
  Connection( TcpManager manager, ITcpListener listener)
  {
    this.manager = manager;
    this.listener = listener;
    
    buffer = ByteBuffer.allocateDirect( 4096);
    buffer.flip();
    largeCount = 25;
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
      largeCount = 25;
      System.out.printf( "Increased buffer: %d\n", buffer.capacity());
    }
  }

  /**
   * Calculate the ongoing usage of the buffer and possibly reduce its size to save memory.
   * TODO: This is broken.
   */
  private void optimizeCapacity()
  {
    float usage = (float)buffer.position() / buffer.limit();
    if ( usage > 0.375f) largeCount++; else largeCount--;

    // limit hysteresis of large messages
    if ( largeCount > 1000) largeCount = 1000;
    
    // contract insufficiently used buffer
    if ( largeCount == 0)
    {
      ByteBuffer smaller = ByteBuffer.allocateDirect( buffer.capacity() >> 1);
      buffer.flip();
      smaller.put( buffer);
      buffer = smaller;
      largeCount = 25;
      System.out.printf( "Decreased buffer: %d\n", buffer.capacity());
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
  private int largeCount;
  private Map<String, IExternalReference> index;
}
