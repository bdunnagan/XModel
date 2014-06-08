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
import java.net.ConnectException;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import org.xmodel.future.AsyncFuture;
import org.xmodel.future.SuccessAsyncFuture;
import org.xmodel.net.nu.IConnectListener;
import org.xmodel.net.nu.IDisconnectListener;
import org.xmodel.net.nu.IErrorListener;
import org.xmodel.net.nu.IReceiveListener;
import org.xmodel.net.nu.ITransport;
import org.xmodel.net.nu.protocol.Protocol;
import org.xmodel.xpath.expression.IContext;

public class TcpClientTransport extends AbstractChannelTransport
{
  public TcpClientTransport( Protocol protocol, IContext transportContext, ScheduledExecutorService scheduler,
      List<IConnectListener> connectListeners, List<IDisconnectListener> disconnectListeners,
      List<IReceiveListener> receiveListeners, List<IErrorListener> errorListeners)
  {
    super( protocol, transportContext, scheduler,
        connectListeners, disconnectListeners,
        receiveListeners, errorListeners);
    
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
  public AsyncFuture<ITransport> connect( int timeout)
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
    bootstrap.option( ChannelOption.AUTO_READ, false);
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
        {
          channelRef.set( channelFuture.channel());
        }
        else
        {
          notifyError( getTransportContext(), translateConnectError( channelFuture.cause()));
        }
        
        super.operationComplete( channelFuture);
      }
    });
    
    return future;
  }
  
  private ITransport.Error translateConnectError( Throwable t)
  {
    if ( t instanceof ConnectException)
    {
      String message = ((ConnectException)t).getMessage();
      if ( message.contains( "refused")) return ITransport.Error.connectRefused;
    }
    
    t.printStackTrace( System.out);
    return ITransport.Error.connectError;
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.nu.ITransport#disconnect()
   */
  @Override
  public AsyncFuture<ITransport> disconnect()
  {
    Channel channel = channelRef.get();
    if ( channel != null) channel.close();
    return new SuccessAsyncFuture<ITransport>( this);
  }
  
  private SocketAddress localAddress;
  private SocketAddress remoteAddress;
}
