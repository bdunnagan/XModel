package org.xmodel.net.nu.tcp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import org.xmodel.future.AsyncFuture;
import org.xmodel.future.SuccessAsyncFuture;
import org.xmodel.net.nu.IProtocol;
import org.xmodel.net.nu.ITransport;
import org.xmodel.xpath.expression.IContext;

public class TcpClientTransport extends AbstractChannelTransport
{
  public TcpClientTransport( IProtocol protocol, IContext transportContext)
  {
    this( protocol, transportContext, null);
  }
  
  public TcpClientTransport( IProtocol protocol, IContext transportContext, ScheduledExecutorService scheduler)
  {
    super( protocol, transportContext, scheduler);
    this.channelRef = new AtomicReference<Channel>();
  }
  
  public void setLocalAddress( SocketAddress address)
  {
    localAddress = address;
  }
  
  
  public void setRemoteAddress( SocketAddress address)
  {
    remoteAddress = address;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.nu.ITransport#connect(int)
   */
  @Override
  public AsyncFuture<ITransport> connect( int timeout) throws IOException
  {
    Channel channel = channelRef.get();
    if ( channel != null && channel.isOpen()) 
      return new SuccessAsyncFuture<ITransport>( this);
    
    // configure
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    
    Bootstrap bootstrap = new Bootstrap(); 
    bootstrap.group( workerGroup); 
    bootstrap.channel( NioSocketChannel.class); 
    bootstrap.option( ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout);
    bootstrap.handler( new ChannelInitializer<SocketChannel>() {
      @Override
      public void initChannel( SocketChannel channel) throws Exception 
      {
        channel.pipeline().addLast( new XioInboundHandler( TcpClientTransport.this));
      }
    });

    // connect
    final ChannelFuture channelFuture = (localAddress != null)? bootstrap.connect( remoteAddress, localAddress): bootstrap.connect( remoteAddress);
    
    AsyncFuture<ITransport> future = new AsyncFuture<ITransport>( this) {
      @Override 
      public void cancel()
      {
        channelFuture.cancel( false);
      }
    };

    channelFuture.addListener( new AsyncFutureAdapter<ITransport>( future) {
      @Override
      public void operationComplete( ChannelFuture channelFuture) throws Exception
      {
        if ( channelFuture.isSuccess())
          channelRef.set( channelFuture.channel());
        super.operationComplete( channelFuture);
      }
    });
    
    return future;
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.nu.ITransport#disconnect()
   */
  @Override
  public AsyncFuture<ITransport> disconnect() throws IOException
  {
    Channel channel = channelRef.get();
    if ( channel != null) channel.close();
    return new SuccessAsyncFuture<ITransport>( this);
  }
  
  private SocketAddress localAddress;
  private SocketAddress remoteAddress;
}
