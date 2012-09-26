package org.xmodel.net.netty;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.xmodel.log.SLog;

/**
 * Experimental echo server.
 */
public class EchoServer extends SimpleChannelHandler
{
  /* (non-Javadoc)
   * @see org.jboss.netty.channel.SimpleChannelHandler#messageReceived(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.MessageEvent)
   */
  @Override
  public void messageReceived( ChannelHandlerContext ctx, MessageEvent e) throws Exception
  {
    e.getChannel().write( e.getMessage());
  }

  /* (non-Javadoc)
   * @see org.jboss.netty.channel.SimpleChannelHandler#exceptionCaught(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ExceptionEvent)
   */
  @Override
  public void exceptionCaught( ChannelHandlerContext ctx, ExceptionEvent e) throws Exception
  {
    SLog.exception( this, e.getCause());
    e.getChannel().close();
  }

  public static void main( String[] args) throws Exception
  {
    ChannelFactory factory = new NioServerSocketChannelFactory( Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
    ServerBootstrap bootstrap = new ServerBootstrap( factory);

    bootstrap.setPipelineFactory( new ChannelPipelineFactory() {
      public ChannelPipeline getPipeline()
      {
        return Channels.pipeline( new EchoServer());
      }
    });

    bootstrap.setOption( "child.tcpNoDelay", true);
    bootstrap.setOption( "child.keepAlive", true);

    bootstrap.bind( new InetSocketAddress( 8080));
  }
}
