package org.xmodel.net;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;
import org.xmodel.log.Log;

public class Heartbeat extends IdleStateAwareChannelHandler
{
  /**
   * Create a heartbeat handler.
   * @param server True if this is a server.
   */
  public Heartbeat( boolean server)
  {
    this.server = server;
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
          if ( server)
          {
            log.debugf( "Sending idle heartbeat to %s", peer.getRemoteAddress());
            //peer.heartbeat();
          }
          break;
        }
      }
    }
  }

  private final static Log log = Log.getLog( Heartbeat.class);
  
  private XioPeer peer;
  private boolean server;
}
