package org.xmodel.net;

import java.net.SocketAddress;

import org.jboss.netty.handler.ssl.SslHandler;
import org.xmodel.net.connection.INetworkConnection;

public interface IXioChannel extends INetworkConnection
{
  /**
   * @return Returns the peer of this connection.
   */
  public XioPeer getPeer();
  
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
}
