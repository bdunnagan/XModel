package org.xmodel.net.stream;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

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
    if ( buffer == null) 
    {
      buffer = ByteBuffer.allocateDirect( 4096);
      buffer.flip();
    }
    
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
    
    buffer.compact();
    
    int nread = channel.read( buffer);
    if ( nread == -1) return nread;
    
    buffer.flip();
    
    if ( listener != null && nread > 0) 
    {
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

  private final static Log log = Log.getLog( "org.xmodel.net.stream");

  private TcpManager manager;
  private ITcpListener listener;
  private InetSocketAddress address;
  private SocketChannel channel;
  private ByteBuffer buffer;
}
