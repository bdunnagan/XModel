package org.xmodel.net.nu.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.xmodel.net.nu.AbstractTransport;

public class XioInboundHandler extends ChannelInboundHandlerAdapter
{
  public XioInboundHandler( AbstractTransport transport)
  {
    this.transport = transport;
  }
  
  /* (non-Javadoc)
   * @see io.netty.channel.ChannelHandlerAdapter#handlerAdded(io.netty.channel.ChannelHandlerContext)
   */
  @Override
  public void handlerAdded( ChannelHandlerContext ctx) throws Exception
  {
    super.handlerAdded( ctx);
    readBuffer = ctx.alloc().buffer();
  }

  /* (non-Javadoc)
   * @see io.netty.channel.ChannelHandlerAdapter#handlerRemoved(io.netty.channel.ChannelHandlerContext)
   */
  @Override
  public void handlerRemoved( ChannelHandlerContext ctx) throws Exception
  {
    super.handlerRemoved( ctx);
    readBuffer.release();
  }

  /* (non-Javadoc)
   * @see io.netty.channel.ChannelInboundHandlerAdapter#channelRead(io.netty.channel.ChannelHandlerContext, java.lang.Object)
   */
  @Override
  public void channelRead( ChannelHandlerContext ctx, Object message) throws Exception
  {
    // accumulate
    ByteBuf buffer = (ByteBuf)message;
    readBuffer.writeBytes( buffer);
    buffer.release();

    // frame
    int readable = readBuffer.readableBytes(); 
    if ( readable > 4)
    {
      readBuffer.markReaderIndex();
      int length = readBuffer.readInt();
      if ( readable >= length)
      {
        // deliver
        transport.notifyReceive( readBuffer.array(), readBuffer.arrayOffset(), length);
      }
      else
      {
        readBuffer.resetReaderIndex();
      }
    }
  }
  
  private AbstractTransport transport;
  private ByteBuf readBuffer;
}
