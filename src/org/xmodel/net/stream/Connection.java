package org.xmodel.net.stream;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.xmodel.log.SLog;
import org.xmodel.net.ILink;

/**
 * A class that represents a TCP connection.
 */
public final class Connection implements ILink
{
  /**
   * Create a new connection.
   * @param tcp The TcpManager instance.
   * @param channel The channel.
   * @param listener The TCP event listener.
   */
  Connection( TcpBase tcp, SocketChannel channel, ILink.IListener listener)
  {
    this.tcp = tcp;
    this.channel = channel;
    this.listener = listener;
    this.semaphore = new Semaphore( 0);
    
    Socket socket = channel.socket();
    if ( socket != null) address = (InetSocketAddress)socket.getRemoteSocketAddress();
    
    buffer = ByteBuffer.allocateDirect( 4096);
    buffer.flip();
  }
  
  /**
   * @return Returns the remote host address.
   */
  public String getAddress()
  {
    return (address != null)? address.getAddress().getHostAddress(): null;
  }
  
  /**
   * @return Returns the remote host port.
   */
  public int getPort()
  {
    return (address != null)? address.getPort(): 0;
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
    semaphore.release();
  }
  
  /**
   * Wait for the connection to be established.
   * @param timeout The timeout in milliseconds.
   * @return Returns true if the connection was established.
   */
  boolean waitForConnect( int timeout) throws IOException, InterruptedException
  {
    if ( semaphore.tryAcquire( timeout, TimeUnit.MILLISECONDS))
    {
      if ( channel == null || !channel.isOpen())
        throw new IOException( "Connection was not established!");
      return true;
    }
    return false;
  }
  
  /**
   * Close this connection.
   */
  public void close()
  {
    TcpBase.log.debugf( "Closing connection to %s:%d.", getAddress(), getPort());

    tcp.discard( this);
    
    try 
    {
      if ( channel != null) 
      {
        channel.close();
        channel = null;
      }
      
      semaphore.release();
    }
    catch( IOException e)
    {
      SLog.exception( this, e);
    }
    finally
    {
      channel = null;
      address = null;
    }
    
    if ( listener != null) listener.onClose( this);
  }
  
  /**
   * @return Returns true if the connection is still open.
   */
  public boolean isOpen()
  {
    return address != null;
  }

  /**
   * Write the specified bytes to the connection.
   * @param bytes The bytes to be written.
   */
  public void send( byte[] bytes) throws IOException
  {
    tcp.write( channel, bytes);
  }
  
  /**
   * Write the content of the specified buffer to the connection. The content of the buffer is processed
   * by another thread, so the client must discard their reference to the buffer after calling this method.
   * @param buffer The buffer.
   */
  public void send( ByteBuffer buffer) throws IOException
  {
    tcp.write( channel, buffer);
  }
  
  /**
   * Notify listeners of a read event.
   * @param buffer The read buffer.
   */
  protected final void notifyRead( ByteBuffer buffer)
  {
    if ( listener != null)
      listener.onReceive( this, buffer);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return String.format( "%s:%d", getAddress(), getPort());
  }

  private TcpBase tcp;
  private ILink.IListener listener;
  private InetSocketAddress address;
  private SocketChannel channel;
  private Semaphore semaphore;
  
  protected ByteBuffer buffer;
}
