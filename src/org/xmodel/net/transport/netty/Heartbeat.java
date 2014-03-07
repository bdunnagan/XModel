package org.xmodel.net.transport.netty;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;
import org.xmodel.log.Log;
import org.xmodel.net.XioPeer;

public class Heartbeat extends IdleStateAwareChannelHandler
{
  /**
   * @return Returns the Heartbeat instance for the specified channel.
   * @param channel The channel.
   */
  public static Heartbeat getInstance( Channel channel)
  {
    return channel.getPipeline().get( Heartbeat.class);
  }
    
  /* (non-Javadoc)
   * @see org.jboss.netty.handler.timeout.IdleStateAwareChannelHandler#channelIdle(org.jboss.netty.channel.ChannelHandlerContext, 
   * org.jboss.netty.handler.timeout.IdleStateEvent)
   */
  @Override
  public void channelIdle( ChannelHandlerContext ctx, IdleStateEvent event) throws Exception
  {
    peer = (XioPeer)event.getChannel().getAttachment();
    if ( peer != null)
    {
      switch( event.getState())
      {
        case READER_IDLE:
        {
          log.warnf( "Closing peer, %s", peer.getRemoteAddress());
          peer.close(); 
          break;
        }
          
        case ALL_IDLE:
        case WRITER_IDLE:
        {
          log.debugf( "Sending idle heartbeat to %s", peer.getRemoteAddress());
          peer.heartbeat();
          break;
        }
      }
    }
  }

  public static Timer timer = new HashedWheelTimer();

  private final static Log log = Log.getLog( Heartbeat.class);
  
  private XioPeer peer;
}
