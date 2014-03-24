package org.xmodel.net.amqp;

import java.net.SocketAddress;
import java.util.concurrent.ScheduledExecutorService;

import org.jboss.netty.handler.ssl.SslHandler;
import org.xmodel.net.IXioChannel;
import org.xmodel.net.XioPeer;
import org.xmodel.net.connection.INetworkConnectionFactory;
import org.xmodel.net.connection.INetworkProtocol;
import org.xmodel.net.connection.ReliableConnection;

public class AmqpXioChannel extends ReliableConnection implements IXioChannel
{
  public AmqpXioChannel( INetworkProtocol protocol, int lifetime, INetworkConnectionFactory connectionFactory, ScheduledExecutorService scheduler)
  {
    super( protocol, lifetime, connectionFactory, scheduler);
  }
  
  /**
   * Set the peer that owns this channel.
   * @param peer The peer.
   */
  public void setPeer( XioPeer peer)
  {
    this.peer = peer;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.IXioChannel#getPeer()
   */
  @Override
  public XioPeer getPeer()
  {
    return peer;
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.IXioChannel#getSslHandler()
   */
  @Override
  public SslHandler getSslHandler()
  {
    // TODO
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.IXioChannel#getLocalAddress()
   */
  @Override
  public SocketAddress getLocalAddress()
  {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.IXioChannel#getRemoteAddress()
   */
  @Override
  public SocketAddress getRemoteAddress()
  {
    // TODO Auto-generated method stub
    return null;
  }

  private XioPeer peer;
}
