package org.xmodel.net.stream;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.xmodel.log.Log;
import org.xmodel.net.INetFramer;
import org.xmodel.net.INetReceiver;
import org.xmodel.net.INetSender;

/**
 * A class that represents a TCP connection.
 */
public class Connection implements INetSender
{
  public interface IListener
  {
    /**
     * Called when a connection is established.
     * @param connection The connection.
     */
    public void connected( Connection connection);
    
    /**
     * Called when a connection is closed.
     * @param connection The connection.
     */
    public void disconnected( Connection connection);
  }
  
  /**
   * Create a new connection.
   * @param agent The agent that created the connection.
   * @param framer The message framer.
   * @param receiver The primary receiver.
   * @param listener The connection listener.
   */
  Connection( ITcpAgent agent, INetFramer framer, INetReceiver receiver, IListener listener)
  {
    this.agent = agent;
    this.framer = framer;
    this.receiver = receiver;
    this.listener = listener;
  }
  
  /**
   * @return Returns the agent that created this connection.
   */
  public ITcpAgent getAgent()
  {
    return agent;
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
    if ( recvBuffer == null) recvBuffer = ByteBuffer.allocateDirect( 4096);
    
    if ( listener != null) listener.connected( this);
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
    
    if ( listener != null) listener.disconnected( this);
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
    int nread = channel.read( recvBuffer);
    recvBuffer.flip();
    
    while( true)
    {
      recvBuffer.mark();

      try
      {
        int consume = framer.frame( recvBuffer);
        int limit = recvBuffer.limit();
        if ( limit > consume)
        {
          // set limit to end of message
          recvBuffer.limit( recvBuffer.position() + consume);
          
          // pass message to receivers
          recvBuffer.reset();
          
          System.out.printf( "read: ");
          for( int i=recvBuffer.position(); i<recvBuffer.limit(); i++)
            System.out.printf(  "%02X", recvBuffer.get( i));
          System.out.println( "");
          
          receiver.receive( this, recvBuffer);
          
          // restore buffer limit and compact
          recvBuffer.limit( limit);
        }
        else
        {
          break;
        }
      }
      catch( BufferUnderflowException e)
      {
        break;
      }
    }
    
    recvBuffer.reset();
    recvBuffer.compact();
    
    return nread;
  }

  /**
   * Synchronously write the content of the specified buffer to the connection.
   * @param buffer The buffer.
   */
  public void write( ByteBuffer buffer) throws IOException
  {
    System.out.printf( "write: pos=%d, lim=%d: ", buffer.position(), buffer.limit());
    for( int i=buffer.position(); i<buffer.limit(); i++) System.out.printf( "%02X", buffer.get( i));
    System.out.println( "");
    
    if ( channel != null) channel.write( buffer);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.nu.INetSender#send(java.nio.ByteBuffer)
   */
  @Override
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

  /* (non-Javadoc)
   * @see org.xmodel.net.nu.INetSender#send(java.nio.ByteBuffer, int)
   */
  @Override
  public ByteBuffer send( ByteBuffer buffer, int timeout)
  {
    syncBuffer = null;
    send( buffer);
    try
    {
      agent.process( timeout);
      return syncBuffer;
    }
    catch( Exception e)
    {
      log.exception( e);
    }
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.nu.INetSender#close()
   */
  @Override
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

  private ITcpAgent agent;
  private INetFramer framer;
  private INetReceiver receiver;
  private InetSocketAddress address;
  private SocketChannel channel;
  private ByteBuffer recvBuffer;
  private ByteBuffer syncBuffer;
  private IListener listener;
  private Log log = Log.getLog( "org.xmodel.net.stream");
}
