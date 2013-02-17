package org.xmodel.net;

import java.net.InetSocketAddress;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

public class XioServerPeer extends XioPeer
{
  public XioServerPeer( InetSocketAddress address)
  {
    this.address = address;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.XioPeer#reconnect()
   */
  @Override
  protected ChannelFuture reconnect()
  {
  }
  
  protected void connected( Channel channel)
  {
    setChannel( channel);
  }
  
  protected void disconnected()
  {
  }
  
  private InetSocketAddress address;
}
