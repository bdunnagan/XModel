package org.xmodel.net.nu.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import org.xmodel.net.nu.ITransportImpl;

public class XioInboundHandler extends ChannelInboundHandlerAdapter
{
  public XioInboundHandler( ITransportImpl transport)
  {
    this.transport = transport;
  }
  
  @Override
  public void handlerAdded( ChannelHandlerContext ctx) throws Exception
  {
    super.handlerAdded( ctx);
    readBuffer = ctx.alloc().buffer();
  }

  @Override
  public void handlerRemoved( ChannelHandlerContext ctx) throws Exception
  {
    super.handlerRemoved( ctx);
    readBuffer.release();
  }

  @Override
  public void channelActive( ChannelHandlerContext ctx) throws Exception
  {
    super.channelActive( ctx);
    transport.getEventPipe().notifyConnect( transport.getTransportContext());
    ctx.read();
  }

  @Override
  public void channelInactive( ChannelHandlerContext ctx) throws Exception
  {
    super.channelInactive( ctx);
    transport.getEventPipe().notifyDisconnect( transport.getTransportContext());
  }

  @Override
  public void channelReadComplete( ChannelHandlerContext ctx) throws Exception
  {
    ctx.read();
  }

  @Override
  public void channelRead( ChannelHandlerContext ctx, Object message) throws Exception
  {
    // accumulate
    ByteBuf buffer = (ByteBuf)message;
    readBuffer.writeBytes( buffer);
    buffer.release();

    // prepare for incomplete message
    readBuffer.markReaderIndex();
    
    // read next message
    if ( !transport.getEventPipe().notifyReceive( readBuffer.nioBuffer()))
    {
      // incomplete message
      readBuffer.resetReaderIndex();
    }
  }
  
  private ITransportImpl transport;
  private ByteBuf readBuffer;
}
