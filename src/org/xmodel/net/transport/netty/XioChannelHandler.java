package org.xmodel.net.transport.netty;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.xmodel.log.Log;
import org.xmodel.log.SLog;
import org.xmodel.net.XioChannel;
import org.xmodel.net.XioPeer;

/**
 * ChannelHandler responsible for the accumulation buffer and decoding header of all messages and routing 
 * to ChannelHandler for the specified message type.
 */
public class XioChannelHandler extends SimpleChannelHandler
{
  public interface IListener
  {
    /**
     * Called when the peer connects.
     * @param peer The peer.
     */
    public void notifyConnect( XioPeer peer);
    
    /**
     * Called when the peer disconnects.
     * @param peer The peer.
     */
    public void notifyDisconnect( XioPeer peer);
  }    
  
  public XioChannelHandler()
  {
    this.listeners = new ArrayList<IListener>( 1);
    buffer = ChannelBuffers.dynamicBuffer();
  }
  
//  public XioChannelHandler( XioChannelHandler handler)
//  {
//    this( handler.bindProtocol.context, handler.executionProtocol.executor, handler.executionProtocol.scheduler, handler.registry);
//  }
  
  /**
   * If this handler is being used by a client, specify the client instance.
   * @param client The client instance.
   */
  public void setClient( NettyXioClient client)
  {
    this.client = client;
  }
  
  /**
   * Add a connection listener.
   * @param listener The listener.
   */
  public void addListener( IListener listener)
  {
    if ( !listeners.contains( listener))
      listeners.add( listener);
  }
  
  /**
   * Remove a connection listener.
   * @param listener The listener.
   */
  public void removeListener( IListener listener)
  {
    listeners.remove( listener);
  }
  
  /**
   * @return Returns null or the handshake future.
   */
  public ChannelFuture getSSLHandshakeFuture()
  {
    return sslHandshakeFuture;
  }
  
  /* (non-Javadoc)
   * @see org.jboss.netty.channel.SimpleChannelHandler#channelConnected(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ChannelStateEvent)
   */
  @Override
  public void channelConnected( ChannelHandlerContext ctx, ChannelStateEvent event) throws Exception
  {
    Channel channel = event.getChannel();
    
    XioPeer peer = null;
    if ( this.client != null)
    {
      // client-side connection
      peer = this.client;
      peer.setChannel( new NettyXioChannel( channel));
    }
    else
    {
      // server-side connection
      NettyXioServer server = (NettyXioServer)channel.getParent().getAttachment();
      peer = server.createConnectionPeer( channel);
    }
    
    channel.setAttachment( peer);
    
    for( IListener listener: listeners)
    {
      try
      {
        listener.notifyConnect( peer);
      }
      catch( Exception e)
      {
        SLog.exception( this, e);
      }
    }
  }

  /* (non-Javadoc)
   * @see org.jboss.netty.channel.SimpleChannelHandler#channelDisconnected(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ChannelStateEvent)
   */
  @Override
  public void channelDisconnected( ChannelHandlerContext ctx, ChannelStateEvent event) throws Exception
  {
    for( IListener listener: listeners)
    {
      try
      {
        XioPeer peer = (XioPeer)event.getChannel().getAttachment();
        peer.reset();
        listener.notifyDisconnect( peer);
      }
      catch( Exception e)
      {
        SLog.exception( this, e);
      }
    }
    
    event.getChannel().setAttachment( null);
  }

  /* (non-Javadoc)
   * @see org.jboss.netty.channel.SimpleChannelHandler#writeRequested(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.MessageEvent)
   */
  @Override
  public void writeRequested( ChannelHandlerContext ctx, MessageEvent e) throws Exception
  {
    ChannelBuffer buffer = (ChannelBuffer)e.getMessage();
    if ( log.verbose()) log.verbosef( "writeRequested: offset=%d\n%s", buffer.readerIndex(), XioPeer.toString( "  ", buffer));
    
    super.writeRequested( ctx, e);
  }

  /* (non-Javadoc)
   * @see org.jboss.netty.channel.SimpleChannelHandler#messageReceived(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.MessageEvent)
   */
  @Override
  public void messageReceived( ChannelHandlerContext chc, MessageEvent event) throws Exception
  {
    Channel channel = event.getChannel();
    XioPeer peer = (XioPeer)channel.getAttachment();
    
    // transfer receive buffer to the accumulation buffer
    buffer.writeBytes( (ChannelBuffer)event.getMessage());
    
    // process messages in buffer
    while( true)
    {
      // store current position of buffer
      int readerIndex = buffer.readerIndex();

      // process next message
      if ( !handleMessage( peer.getChannel(), buffer))
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
  private boolean handleMessage( XioChannel channel, ChannelBuffer buffer) throws Exception
  {
    return channel.getPeer().handleMessage( channel, buffer);
  }
  
  /* (non-Javadoc)
   * @see org.jboss.netty.channel.SimpleChannelHandler#exceptionCaught(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ExceptionEvent)
   */
  @Override
  public void exceptionCaught( ChannelHandlerContext context, ExceptionEvent event) throws Exception
  {
    Throwable t = event.getCause();
    if ( t != null)
    {
      if ( t instanceof ConnectException)
      {
        log.error( t.toString());
      }
      else
      {
        log.exception( t);
      }
    }
  }

  private final static Log log = Log.getLog( XioChannelHandler.class);
  
  private ChannelBuffer buffer;
  private ChannelFuture sslHandshakeFuture;
  private XioPeer client;
  private List<IListener> listeners;
}
