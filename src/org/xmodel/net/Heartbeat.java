package org.xmodel.net;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;
import org.xmodel.log.Log;

public class Heartbeat extends IdleStateAwareChannelHandler
{
  /**
   * Create a heartbeat handler.
   * @param enabled True if hearbeat monitoring is initially enabled.
   */
  public Heartbeat( boolean enabled)
  {
    this.enabled = enabled;
  }
  
  /**
   * When enabled, this handler will close the connection when a heartbeat is not received.
   * @param enable True if heartbeat monitoring should be enabled.
   */
  public void setEnabled( boolean enabled)
  {
    if ( this.enabled != enabled)
    {
      log.debugf( "Heartbeat monitoring is %s", enabled? "enabled": "disabled");
      this.enabled = enabled;
    }
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
          if ( enabled) 
          {
            log.warnf( "Closing peer, %s", peer.getRemoteAddress());
            peer.close(); 
          }
          break;
        }
          
        case ALL_IDLE:
        case WRITER_IDLE:
        {
          if ( enabled)
          {
            log.debugf( "Sending idle heartbeat to %s", peer.getRemoteAddress());
            peer.heartbeat();
          }
          break;
        }
      }
    }
  }

  public static Timer timer = new HashedWheelTimer();

  private final static Log log = Log.getLog( Heartbeat.class);
  
  private XioPeer peer;
  private boolean enabled;
}
