package org.xmodel.net.execution;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

public class RemoteExecutionDecoder extends FrameDecoder
{
  /* (non-Javadoc)
   * @see org.jboss.netty.handler.codec.frame.FrameDecoder#decode(org.jboss.netty.channel.ChannelHandlerContext, 
   * org.jboss.netty.channel.Channel, org.jboss.netty.buffer.ChannelBuffer)
   */
  @Override
  protected Object decode( ChannelHandlerContext context, Channel channel, ChannelBuffer buffer) throws Exception
  {
    int length = RemoteExecutionSession.readLength( buffer);
    return (length > 0)? buffer.readBytes( length): null;
  }
}