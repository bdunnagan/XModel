package org.xmodel.net.execution;

import java.util.Map;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.xmodel.xpath.expression.IContext;

/**
 */
public class RemoteExecutionServer extends SimpleChannelHandler
{
  /**
   * Set the execution context for the server.
   * @param context The execution context.
   */
  public void setExecutionContext( IContext context)
  {
    this.context = context;
  }
  
  /* (non-Javadoc)
   * @see org.jboss.netty.channel.SimpleChannelHandler#channelConnected(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ChannelStateEvent)
   */
  @Override
  public void channelConnected( ChannelHandlerContext chc, ChannelStateEvent event) throws Exception
  {
    RemoteExecutionSession session = new RemoteExecutionSession( event.getChannel(), context);
    sessions.put( event.getChannel(), session);
  }

  /* (non-Javadoc)
   * @see org.jboss.netty.channel.SimpleChannelHandler#channelDisconnected(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ChannelStateEvent)
   */
  @Override
  public void channelDisconnected( ChannelHandlerContext chc, ChannelStateEvent event) throws Exception
  {
    sessions.remove( event.getChannel());
  }
  
  /* (non-Javadoc)
   * @see org.jboss.netty.channel.SimpleChannelHandler#messageReceived(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.MessageEvent)
   */
  @Override
  public void messageReceived( ChannelHandlerContext chc, MessageEvent event) throws Exception
  {
    RemoteExecutionSession session = sessions.get( event.getChannel());
    session.handleRequest( (ChannelBuffer)event.getMessage());
  }
  
  /* (non-Javadoc)
   * @see org.jboss.netty.channel.SimpleChannelHandler#exceptionCaught(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ExceptionEvent)
   */
  @Override
  public void exceptionCaught( ChannelHandlerContext context, ExceptionEvent event) throws Exception
  {
  }

  private IContext context;
  private Map<Channel, RemoteExecutionSession> sessions;
}
