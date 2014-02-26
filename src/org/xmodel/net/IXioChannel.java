package org.xmodel.net;

import java.net.InetSocketAddress;

import org.jboss.netty.buffer.ChannelBuffer;
import org.xmodel.future.AsyncFuture;

/**
 * An abstraction of a communications channel.
 */
public interface IXioChannel
{
  /**
   * @return Returns the peer of this connection.
   */
  public XioPeer getPeer();
  
  /**
   * @return Returns true if the channel is connected.
   */
  public boolean isConnected();
  
  /**
   * Send the content of the specified buffer.
   * @param buffer The buffer.
   */
  public void write( ChannelBuffer buffer);
  
  /**
   * @return Returns the local address of the connection.
   */
  public InetSocketAddress getLocalAddress();
  
  /**
   * @return Returns the remote address of the connection.
   */
  public InetSocketAddress getRemoteAddress();
  
  /**
   * @return Returns the future that will be notified when the channel is closed.
   */
  public AsyncFuture<IXioChannel> getCloseFuture();
}
