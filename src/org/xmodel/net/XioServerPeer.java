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
    synchronized( this)
    {
      this.future = new ClientConnectionFuture();
    }
    
    
  }
  
  protected void connected( Channel channel)
  {
    ChannelFuture future = null;
    
    synchronized( this)
    {
      setChannel( channel);
      future = this.future;
    }
    
    if ( future != null) future.setSuccess();
  }
  
  protected void disconnected()
  {
  }
  
  private InetSocketAddress address;
  private ChannelFuture future;
}
