package org.xmodel.net.nu;

import java.util.concurrent.ScheduledExecutorService;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.xmodel.IDispatcher;
import org.xmodel.log.Log;
import org.xmodel.net.bind.BindProtocol;
import org.xmodel.net.execution.ExecutionProtocol;
import org.xmodel.net.execution.ExecutionRequestProtocol;
import org.xmodel.net.execution.ExecutionResponseProtocol;
import org.xmodel.xpath.expression.IContext;

/**
 * ChannelHandler responsible for the accumulation buffer and decoding header of all messages and routing 
 * to ChannelHandler for the specified message type.
 */
public class FullProtocolChannelHandler extends SimpleChannelHandler
{
  public FullProtocolChannelHandler( IContext context, IDispatcher dispatcher, ScheduledExecutorService scheduler)
  {
    headerProtocol = new HeaderProtocol();
    executionProtocol = new ExecutionProtocol( headerProtocol, errorProtocol, context, dispatcher, scheduler);
    bindProtocol = new BindProtocol( headerProtocol, errorProtocol, context, dispatcher);
  }
  
  /**
   * Message type field (must be less than 32).
   */
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
    changeDirty,
    error
  }
  
  /* (non-Javadoc)
   * @see org.jboss.netty.channel.SimpleChannelHandler#channelConnected(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ChannelStateEvent)
   */
  @Override
  public void channelConnected( ChannelHandlerContext chc, ChannelStateEvent event) throws Exception
  {
  }

  /* (non-Javadoc)
   * @see org.jboss.netty.channel.SimpleChannelHandler#channelDisconnected(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ChannelStateEvent)
   */
  @Override
  public void channelDisconnected( ChannelHandlerContext chc, ChannelStateEvent event) throws Exception
  {
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
    
    // make sure at least one byte in buffer
    if ( buffer.readableBytes() == 0) return;

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
  }
  
  /**
   * Read the next message from the buffer and pass it on for processing.
   * @param channel The channel.
   * @param buffer The buffer.
   * @return Returns true if a message was read.
   */
  private boolean handleMessage( Channel channel, ChannelBuffer buffer)
  {
    Type type = headerProtocol.readType( buffer);
    log.debugf( "Type: %s", type);
    
    long length = headerProtocol.readLength( buffer);
    if ( buffer.readableBytes() < length) return false;
    
    switch( type)
    {
      case executeRequest:  executionProtocol.requestProtocol.handle( channel, buffer); break;
      case executeResponse: executionProtocol.responseProtocol.handle( channel, buffer); break;
      
      case bindRequest:     bindProtocol.bindRequestProtocol.handle( channel, buffer, length); break;
      case bindResponse:    bindProtocol.bindResponseProtocol.handle( channel, buffer); break;
      case unbindRequest:   bindProtocol.unbindRequestProtocol.handle( channel, buffer); break;
      
      case syncRequest:     bindProtocol.syncRequestProtocol.handle( channel, buffer); break;
      case syncResponse:    bindProtocol.syncResponseProtocol.handle( channel, buffer); break;
      
      case addChild:        bindProtocol.updateProtocol.handleAddChild( channel, buffer); break;
      case removeChild:     bindProtocol.updateProtocol.handleRemoveChild( channel, buffer); break;
      case changeAttribute: bindProtocol.updateProtocol.handleChangeAttribute( channel, buffer); break;
      case clearAttribute:  bindProtocol.updateProtocol.handleClearAttribute( channel, buffer); break;
      case changeDirty:     bindProtocol.updateProtocol.handleChangeDirty( channel, buffer); break;
      
      case error:           errorProtocol.handleError( channel, buffer); break;
    }
  }
  
  /* (non-Javadoc)
   * @see org.jboss.netty.channel.SimpleChannelHandler#exceptionCaught(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ExceptionEvent)
   */
  @Override
  public void exceptionCaught( ChannelHandlerContext context, ExceptionEvent event) throws Exception
  {
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

  private final static Log log = Log.getLog( FullProtocolChannelHandler.class);
  
  private ChannelBuffer buffer;
  private HeaderProtocol headerProtocol;
  private ExecutionProtocol executionProtocol;
  private BindProtocol bindProtocol;
  private ErrorProtocol errorProtocol;
}
