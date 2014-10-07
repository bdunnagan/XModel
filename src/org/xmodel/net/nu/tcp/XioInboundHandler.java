package org.xmodel.net.nu.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.nio.ByteBuffer;
import org.xmodel.log.SLog;
import org.xmodel.net.nu.ITransportImpl;
import org.xmodel.util.HexDump;

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
    readBuffer = ByteBuffer.allocate( 1024);
  }

  @Override
  public void handlerRemoved( ChannelHandlerContext ctx) throws Exception
  {
    super.handlerRemoved( ctx);
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
    ByteBuf buffer = (ByteBuf)message;
    
    int shortage = buffer.readableBytes() - readBuffer.remaining();
    if ( shortage > 0)
    {
      System.out.printf( "shortage=%d\n", shortage);
      readBuffer.flip();
      ByteBuffer newBuffer = ByteBuffer.allocate( readBuffer.capacity() + (shortage * 2));
      newBuffer.put( readBuffer);
      readBuffer = newBuffer;
    }
    
    //System.out.printf( "put: position=%d, limit=%d\n", readBuffer.position(), readBuffer.limit());
    int readableBytes = buffer.readableBytes();
    buffer.readBytes( readBuffer.array(), readBuffer.position(), readableBytes);
    readBuffer.position( readBuffer.position() + readableBytes);
    readBuffer.limit( readBuffer.capacity());
    readBuffer.flip();
    
    //System.out.printf( "get: position=%d, limit=%d\n", readBuffer.position(), readBuffer.limit());
    System.out.println( HexDump.toString( Unpooled.wrappedBuffer( readBuffer)));
    
    buffer.release();

    while( readBuffer.hasRemaining())
    {
      readBuffer.mark();
      if ( !transport.getEventPipe().notifyReceive( readBuffer))
      {
        readBuffer.reset();
        break;
      }
    }
    
    readBuffer.compact();
  }
  
  @Override
  public void exceptionCaught( ChannelHandlerContext ctx, Throwable cause) throws Exception
  {
    SLog.exceptionf( this, cause, "Unhandled exception ...");
  }

  private ITransportImpl transport;
  private ByteBuffer readBuffer;
}
