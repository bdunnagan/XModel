package org.xmodel.net.transport.netty;

import java.net.SocketAddress;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.handler.ssl.SslHandler;
import org.xmodel.future.AsyncFuture;
import org.xmodel.net.IXioChannel;
import org.xmodel.net.XioPeer;
import org.xmodel.net.XioServer;
import org.xmodel.net.bind.BindProtocol;
import org.xmodel.net.echo.EchoProtocol;
import org.xmodel.net.execution.ExecutionProtocol;
import org.xmodel.net.register.RegisterProtocol;

public final class NettyXioChannel implements IXioChannel
{
  public NettyXioChannel( Channel channel)
  {
    this.channel = channel;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.IXioChannel#getPeer()
   */
  @Override
  public XioPeer getPeer()
  {
    return (XioPeer)channel.getAttachment();
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.IXioChannel#getServer()
   */
  @Override
  public XioServer getServer()
  {
    return (XioServer)channel.getParent().getAttachment();
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.IXioChannel#close()
   */
  @Override
  public AsyncFuture<IXioChannel> close()
  {
    final AsyncFuture<IXioChannel> future = getCloseFuture();
    
    ChannelFuture channelFuture = channel.close();
    channelFuture.addListener( new ChannelFutureListener() {
      public void operationComplete( ChannelFuture channelFuture) throws Exception
      {
        if ( channelFuture.isSuccess()) future.notifySuccess(); else future.notifyFailure( channelFuture.getCause());
      }
    });
    
    return future;
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.IXioChannel#getSslHandler()
   */
  @Override
  public SslHandler getSslHandler()
  {
    return channel.getPipeline().get( SslHandler.class);
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.IXioChannel#getEchoProtocol()
   */
  @Override
  public EchoProtocol getEchoProtocol()
  {
    XioChannelHandler handler = channel.getPipeline().get( XioChannelHandler.class);
    return handler.getEchoProtocol();
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.IXioChannel#getBindProtocol()
   */
  @Override
  public BindProtocol getBindProtocol()
  {
    XioChannelHandler handler = channel.getPipeline().get( XioChannelHandler.class);
    return handler.getBindProtocol();
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.IXioChannel#getExecuteProtocol()
   */
  @Override
  public ExecutionProtocol getExecuteProtocol()
  {
    XioChannelHandler handler = channel.getPipeline().get( XioChannelHandler.class);
    return handler.getExecuteProtocol();
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.IXioChannel#getRegisterProtocol()
   */
  @Override
  public RegisterProtocol getRegisterProtocol()
  {
    XioChannelHandler handler = channel.getPipeline().get( XioChannelHandler.class);
    return handler.getRegisterProtocol();
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.IXioChannel#isConnected()
   */
  @Override
  public boolean isConnected()
  {
    return channel.isConnected();
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.IXioChannel#write(org.jboss.netty.buffer.ChannelBuffer)
   */
  @Override
  public void write( ChannelBuffer buffer)
  {
    channel.write( buffer);
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.IXioChannel#getLocalAddress()
   */
  @Override
  public SocketAddress getLocalAddress()
  {
    return channel.getLocalAddress();
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.IXioChannel#getRemoteAddress()
   */
  @Override
  public SocketAddress getRemoteAddress()
  {
    return channel.getRemoteAddress();
  }

  @Override
  public synchronized AsyncFuture<IXioChannel> getCloseFuture()
  {
    if ( closeFuture == null) closeFuture = new AsyncFuture<IXioChannel>( this);
    return closeFuture;
  }

  private Channel channel;
  private AsyncFuture<IXioChannel> closeFuture;
}
