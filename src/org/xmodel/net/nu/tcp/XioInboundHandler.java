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
    readBuffer = ByteBuffer.allocate( 4096);
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
    //
    // Read buffer position should point to write position,
    // and limit should be capacity.
    //
    int readPos = readBuffer.position();
    
    // transfer bytes read to read buffer
    ByteBuf buffer = (ByteBuf)message;
    readBuffer.put( buffer.nioBuffer());
    buffer.release();

    //
    // Read buffer position should point to read position,
    // and limit should be just after the last readable byte.
    //
    readBuffer.limit( readBuffer.position());
    readBuffer.position( readPos);

    System.out.println( HexDump.toString( Unpooled.wrappedBuffer( readBuffer)));
    
    // deliver all messages in buffer
    while( true)
    {
      // prepare for incomplete message
      readBuffer.mark();
          
      // read next message
      if ( !transport.getEventPipe().notifyReceive( readBuffer))
      {
        // incomplete message
        readBuffer.reset();
        
        break;
      }
    }

    readBuffer.limit( readBuffer.capacity());
  }
  
  @Override
  public void exceptionCaught( ChannelHandlerContext ctx, Throwable cause) throws Exception
  {
    SLog.exceptionf( this, cause, "Unhandled exception ...");
  }

  private ITransportImpl transport;
  private ByteBuffer readBuffer;
}
