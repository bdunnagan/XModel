package org.xmodel.net;

import java.util.concurrent.ScheduledExecutorService;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.xmodel.log.Log;
import org.xmodel.net.bind.BindProtocol;
import org.xmodel.net.execution.ExecutionProtocol;
import org.xmodel.xpath.expression.IContext;

/**
 * ChannelHandler responsible for the accumulation buffer and decoding header of all messages and routing 
 * to ChannelHandler for the specified message type.
 */
public class XioChannelHandler extends SimpleChannelHandler
{
  public enum Type
  {
    executeRequest,
    executeResponse,
    bindRequest,
    bindResponse,
    unbindRequest,
    syncRequest,
    syncResponse,
    addChild,
    removeChild,
    changeAttribute,
    clearAttribute,
    changeDirty
  }
  
  public XioChannelHandler( IContext bindContext, IContext executeContext, ScheduledExecutorService scheduler)
  {
    headerProtocol = new HeaderProtocol();
    bindProtocol = new BindProtocol( headerProtocol, bindContext);
    executionProtocol = new ExecutionProtocol( headerProtocol, executeContext, scheduler);
    buffer = ChannelBuffers.dynamicBuffer();
  }
  
  public XioChannelHandler( XioChannelHandler handler)
  {
    this( handler.bindProtocol.context, handler.executionProtocol.context, handler.executionProtocol.scheduler);
  }
  
  /**
   * @return Returns the protocol object that implements remote bind.
   */
  public BindProtocol getBindProtocol()
  {
    return bindProtocol;
  }
  
  /**
   * @return Returns the protocol that implements remote execution.
   */
  public ExecutionProtocol getExecuteProtocol()
  {
    return executionProtocol;
  }

  /* (non-Javadoc)
   * @see org.jboss.netty.channel.SimpleChannelHandler#channelConnected(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ChannelStateEvent)
   */
  @Override
  public void channelConnected( ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception
  {
//    final SslHandler sslHandler = ctx.getPipeline().get( SslHandler.class);
//
//    ChannelFuture handshakeFuture = sslHandler.handshake();
//    handshakeFuture.addListener( new Greeter( sslHandler));
  }

  /* (non-Javadoc)
   * @see org.jboss.netty.channel.SimpleChannelHandler#channelDisconnected(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ChannelStateEvent)
   */
  @Override
  public void channelDisconnected( ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception
  {
    bindProtocol.reset();
    executionProtocol.reset();
  }

  /* (non-Javadoc)
   * @see org.jboss.netty.channel.SimpleChannelHandler#writeRequested(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.MessageEvent)
   */
  @Override
  public void writeRequested( ChannelHandlerContext ctx, MessageEvent e) throws Exception
  {
    ChannelBuffer buffer = (ChannelBuffer)e.getMessage();
    if ( log.verbose()) log.verbosef( "writeRequested: offset=%d\n%s", buffer.readerIndex(), toString( "  ", buffer));
    
    super.writeRequested( ctx, e);
  }

  /* (non-Javadoc)
   * @see org.jboss.netty.channel.SimpleChannelHandler#messageReceived(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.MessageEvent)
   */
  @Override
  public void messageReceived( ChannelHandlerContext chc, MessageEvent event) throws Exception
  {
    Channel channel = event.getChannel();
    
    // transfer receive buffer to the accumulation buffer
    buffer.writeBytes( (ChannelBuffer)event.getMessage());
    
    // process messages in buffer
    while( true)
    {
      // store current position of buffer
      int readerIndex = buffer.readerIndex();

      // process next message
      if ( !handleMessage( channel, buffer))
      {
        buffer.readerIndex( readerIndex);
        break;
      }
    }
    
    buffer.discardReadBytes();
  }
  
  /**
   * Read the next message from the buffer and pass it on for processing.
   * @param channel The channel.
   * @param buffer The buffer.
   * @return Returns true if a message was read.
   */
  private boolean handleMessage( Channel channel, ChannelBuffer buffer) throws Exception
  {
    if ( log.verbose()) log.verbosef( "handleMessage: offset=%d\n%s", buffer.readerIndex(), toString( "  ", buffer));
    
    if ( buffer.readableBytes() < 9) return false;
    
    Type type = headerProtocol.readType( buffer);
    log.debugf( "Message Type: %s", type);
    
    long length = headerProtocol.readLength( buffer);
    if ( buffer.readableBytes() < length) return false;
    log.debugf( "Message Length: %d", length);
    
    switch( type)
    {
      case executeRequest:  executionProtocol.requestProtocol.handle( channel, buffer); return true;
      case executeResponse: executionProtocol.responseProtocol.handle( channel, buffer); return true;
      
      case bindRequest:     bindProtocol.bindRequestProtocol.handle( channel, buffer, length); return true;
      case bindResponse:    bindProtocol.bindResponseProtocol.handle( channel, buffer, length); return true;
      case unbindRequest:   bindProtocol.unbindRequestProtocol.handle( channel, buffer); return true;
      case syncRequest:     bindProtocol.syncRequestProtocol.handle( channel, buffer); return true;
      case syncResponse:    bindProtocol.syncResponseProtocol.handle( channel, buffer); return true;
      case addChild:        bindProtocol.updateProtocol.handleAddChild( channel, buffer); return true;
      case removeChild:     bindProtocol.updateProtocol.handleRemoveChild( channel, buffer); return true;
      case changeAttribute: bindProtocol.updateProtocol.handleChangeAttribute( channel, buffer); return true;
      case clearAttribute:  bindProtocol.updateProtocol.handleClearAttribute( channel, buffer); return true;
      case changeDirty:     bindProtocol.updateProtocol.handleChangeDirty( channel, buffer); return true;
    }
    
    return false;
  }
  
  /* (non-Javadoc)
   * @see org.jboss.netty.channel.SimpleChannelHandler#exceptionCaught(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ExceptionEvent)
   */
  @Override
  public void exceptionCaught( ChannelHandlerContext context, ExceptionEvent event) throws Exception
  {
    log.exception( event.getCause());
//    log.error( event);
  }
  
  /**
   * Dump the content of the specified buffer.
   * @param indent The indentation before each line.
   * @param buffer The buffer.
   * @return Returns a string containing the dump.
   */
  public final static String toString( String indent, ChannelBuffer buffer)
  {
    StringBuilder sb = new StringBuilder();
    sb.append( indent);
    
    int bpl = 64;
    for( int i=0, n=0; i<buffer.readableBytes(); i++)
    {
      if ( n == 0)
      {
        for( int j=0; j<bpl && (i + j) < buffer.readableBytes(); j+=4)
          sb.append( String.format( "|%-8d", i + j));
        sb.append( String.format( "\n%s", indent));
      }
      
      if ( (n % 4) == 0) sb.append( "|");
      sb.append( String.format( "%02x", buffer.getByte( buffer.readerIndex() + i)));
        
      if ( ++n == bpl) 
      { 
        sb.append( String.format( "\n%s", indent));
        n=0;
      }
    }
    
    return sb.toString();
  }

  private final static Log log = Log.getLog( XioChannelHandler.class);
  
  private ChannelBuffer buffer;
  private HeaderProtocol headerProtocol;
  private ExecutionProtocol executionProtocol;
  private BindProtocol bindProtocol;
}
