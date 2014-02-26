package org.xmodel.net;

import java.net.SocketAddress;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.ssl.SslHandler;
import org.xmodel.future.AsyncFuture;
import org.xmodel.net.bind.BindProtocol;
import org.xmodel.net.echo.EchoProtocol;
import org.xmodel.net.execution.ExecutionProtocol;
import org.xmodel.net.register.RegisterProtocol;

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
   * @return Returns null or the server that created this channel.
   */
  public XioServer getServer();
  
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
   * Close this channel.
   * @return Returns a future for the close operation.
   */
  public AsyncFuture<IXioChannel> close();
  
  /**
   * @return Returns null or the SslHandler instance.
   */
  public SslHandler getSslHandler();
  
  /**
   * @return Returns the local address of the connection.
   */
  public SocketAddress getLocalAddress();
  
  /**
   * @return Returns the remote address of the connection.
   */
  public SocketAddress getRemoteAddress();
  
  /**
   * @return Returns the future that will be notified when the channel is closed.
   */
  public AsyncFuture<IXioChannel> getCloseFuture();
  
  /**
   * @return Returns the specified protocol.
   */
  public EchoProtocol getEchoProtocol();
  
  /**
   * @return Returns the specified protocol.
   */
  public BindProtocol getBindProtocol();
  
  /**
   * @return Returns the specified protocol.
   */
  public ExecutionProtocol getExecuteProtocol();
  
  /**
   * @return Returns the specified protocol.
   */
  public RegisterProtocol getRegisterProtocol();
}
