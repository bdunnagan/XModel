package org.xmodel.net.nu;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Connection
{
  /**
   * Create a new connection.
   * @param recipient The recipient.
   */
  Connection( IRecipient recipient)
  {
    this.recipient = recipient;
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
  void connected( SocketChannel channel)
  {
    this.channel = channel;
    this.address = (InetSocketAddress)channel.socket().getRemoteSocketAddress();
    if ( buffer == null) buffer = ByteBuffer.allocateDirect( 256);
    
    if ( recipient != null) recipient.connected( this);
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
    
    if ( recipient != null) recipient.disconnected( this, nice);
  }
  
  /**
   * @return Returns true if the connection is still open.
   */
  public boolean isOpen()
  {
    return channel != null;
  }

  /**
   * Write the content of the specified buffer to the connection.
   * @param buffer The buffer (not yet flipped).
   */
  public void write( ByteBuffer buffer) throws IOException
  {
    buffer.flip();
    channel.write( buffer);
  }
  
  /**
   * @return Returns the number of bytes read.
   */
  int read() throws IOException
  {
    int nread = channel.read( buffer);
    if ( recipient != null) 
    {
      buffer.flip();
      buffer.mark();
      
      int consumed = recipient.received( this, buffer);
      
      buffer.reset();
      buffer.position( consumed);
      buffer.limit( buffer.capacity());
    }
    return nread;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return String.format( "%s:%d", getAddress(), getPort());
  }

  private InetSocketAddress address;
  private SocketChannel channel;
  private ByteBuffer buffer;
  private IRecipient recipient;
}
