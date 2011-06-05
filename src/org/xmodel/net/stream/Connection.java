package org.xmodel.net.stream;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;

import org.xmodel.log.Log;

/**
 * A class that represents a TCP connection.
 */
public class Connection
{
  /**
   * Create a new connection.
   * @param listener The TCP event listener.
   */
  Connection( ITcpListener listener)
  {
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
    
    selector = SelectorProvider.provider().openSelector();
    channel.register( selector, SelectionKey.OP_WRITE);
    
    this.address = (InetSocketAddress)channel.socket().getRemoteSocketAddress();
    if ( buffer == null) buffer = ByteBuffer.allocateDirect( 4096);
    
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
    
    int nread = channel.read( buffer);
    if ( nread == -1) return nread;
    
    if ( listener != null && nread > 0) 
    {
      System.out.printf( "READ\n%s\n", Util.dump( buffer));
      listener.onReceive( this, buffer);
    }
    
    return nread;
  }

  /**
   * Synchronously write the content of the specified buffer to the connection.
   * @param buffer The buffer.
   */
  private void write( ByteBuffer buffer) throws IOException
  {
    System.out.printf( "WRITE\n%s\n", Util.dump( buffer));
    
    if ( channel == null) return;
    
    // wait for channel to become writeable
    if ( selector.select() == 0) throw new IOException( "Write failed.");
    
    // clear write operation
    selector.selectedKeys().clear();
    
    // write
    channel.write( buffer);
  }
  
  /**
   * Send the contents of the specified buffer.
   * @param buffer The buffer.
   * @return Returns true if the send was successful.
   */
  public boolean send( ByteBuffer buffer)
  {
    try
    {
      write( buffer);
      return true;
    }
    catch( IOException e)
    {
      return false;
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

  @SuppressWarnings("unused")
  private final static Log log = Log.getLog( "org.xmodel.net.stream");
  
  private ITcpListener listener;
  private InetSocketAddress address;
  private SocketChannel channel;
  private ByteBuffer buffer;
  private Selector selector;
}
