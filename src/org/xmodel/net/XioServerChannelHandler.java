package org.xmodel.net;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChildChannelStateEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.xmodel.xpath.expression.IContext;

public class XioServerChannelHandler extends SimpleChannelHandler
{
  public XioServerChannelHandler( IContext bindContext, IContext executeContext)
  {
    this.bindContext = bindContext;
    this.executeContext = executeContext;
  }
  
  /* (non-Javadoc)
   * @see org.jboss.netty.channel.SimpleChannelHandler#childChannelOpen(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ChildChannelStateEvent)
   */
  @Override
  public void childChannelOpen( ChannelHandlerContext chc, ChildChannelStateEvent event) throws Exception
  {
    
    
    super.childChannelOpen( chc, event);
  }
  
  /* (non-Javadoc)
   * @see org.jboss.netty.channel.SimpleChannelHandler#childChannelClosed(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ChildChannelStateEvent)
   */
  @Override
  public void childChannelClosed( ChannelHandlerContext chc, ChildChannelStateEvent event) throws Exception
  {
    super.childChannelClosed( chc, event);
  }

  private IContext bindContext;
  private IContext executeContext;
}
