package org.xmodel.net;

import org.jboss.netty.channel.Channel;
import org.xmodel.future.AsyncFuture;

/**
 * An XioPeer for the server-side.  This class will reconnect using the reverse connection address
 * for the client, if it becomes disconnected.
 */
public class XioServerPeer extends XioPeer
{
  public XioServerPeer( XioServer server, String clientHost, Channel channel)
  {
    this.server = server;
    this.clientHost = clientHost;
    
    setReconnect( true);
    setChannel( channel);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.XioPeer#reconnect()
   */
  @Override
  protected AsyncFuture<XioPeer> reconnect()
  {
    return server.getPeerByHost( clientHost);
  }
  
  private XioServer server;
  private String clientHost;
}
