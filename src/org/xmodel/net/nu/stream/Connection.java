package org.xmodel.net.nu.stream;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import org.xmodel.net.nu.INetFramer;

public class Connection
{
  /**
   * Create a new connection.
   * @param agent The agent that created the connection.
   * @param framer The message framer.
   * @param receiver The primary receiver.
   */
  Connection( ITcpAgent agent, INetFramer framer, IReceiver receiver)
  {
    this.agent = agent;
    this.framer = framer;
    receivers = new ArrayList<IReceiver>();
    addReceiver( receiver);
  }
  
  /**
   * @return Returns the agent that created this connection.
   */
  public ITcpAgent getAgent()
  {
    return agent;
  }
  
  /**
   * Add a receiver.
   * @param receiver The receiver.
   */
  public void addReceiver( IReceiver receiver)
  {
    receivers.add( receiver);
  }
  
  /**
   * Remove a receiver.
   * @param receiver The receiver.
   */
  public void removeReceiver( IReceiver receiver)
  {
    receivers.remove( receiver);
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
    int nread = channel.read( buffer);
    buffer.flip();
    buffer.mark();
    
    int consume = framer.frame( buffer);
    buffer.reset();
    
    int limit = buffer.limit();
    if ( limit > consume)
    {
      // set limit to end of message
      buffer.limit( consume);
      
      // pass message to receivers
      for( IReceiver receiver: receivers)
        receiver.received( this, buffer);
      
      // restore buffer limit and compact
      buffer.limit( limit);
      buffer.compact();
    }
    else
    {
      buffer.reset();
    }
    
    return nread;
  }

  /**
   * Write the content of the specified buffer to the connection.
   * @param buffer The buffer.
   */
  public void write( ByteBuffer buffer) throws IOException
  {
    channel.write( buffer);
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
  private InetSocketAddress address;
  private SocketChannel channel;
  private ByteBuffer buffer;
  private List<IReceiver> receivers;
}
