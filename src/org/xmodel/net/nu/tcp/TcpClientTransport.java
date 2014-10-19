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
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

import org.xmodel.IModelObject;
import org.xmodel.future.AsyncFuture;
import org.xmodel.future.SuccessAsyncFuture;
import org.xmodel.log.SLog;
import org.xmodel.net.nu.IRouter;
import org.xmodel.net.nu.ITransport;
import org.xmodel.net.nu.ITransportImpl;
import org.xmodel.net.nu.SimpleRouter;
import org.xmodel.net.nu.protocol.Protocol;
import org.xmodel.xpath.expression.IContext;

public class TcpClientTransport extends AbstractChannelTransport implements IRouter
{
  public TcpClientTransport( Protocol protocol, IContext transportContext)
  {
    super( protocol, transportContext);
    this.channelRef = new AtomicReference<Channel>();
    this.router = new SimpleRouter();
  }

  public void setLocalAddress( SocketAddress address)
  {
    localAddress = address;
  }
  
  public void setRemoteAddress( SocketAddress address)
  {
    remoteAddress = address;
  }

  @Override
  public void setConnectTimeout( int timeout)
  {
    connectTimeout = timeout;
  }

  @Override
  public AsyncFuture<ITransport> connect()
  {
    Channel channel = channelRef.get();
    if ( channel != null && channel.isOpen()) 
      return new SuccessAsyncFuture<ITransport>( this);
    
    // configure
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    
    Bootstrap bootstrap = new Bootstrap(); 
    bootstrap.group( workerGroup); 
    bootstrap.channel( NioSocketChannel.class); 
    bootstrap.option( ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout);
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
          getEventPipe().notifyError( (ITransportImpl)future.getInitiator(), getTransportContext(), translateConnectError( channelFuture.cause()), null);
        }
        
        super.operationComplete( channelFuture);
      }
    });
    
    return future;
  }
  
  @Override
  public void addRoute( String route, ITransport transport)
  {
    router.addRoute( route, transport);
  }

  @Override
  public void removeRoute( String route, ITransport transport)
  {
    router.removeRoute( route, transport);
  }

  @Override
  public void removeRoutes( ITransport transport)
  {
    router.removeRoutes( transport);
  }

  @Override
  public boolean hasRoute( String route)
  {
    return router.hasRoute( route);
  }

  @Override
  public Iterator<ITransport> resolve( String route)
  {
    return router.resolve( route);
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
  
  @Override
  public boolean notifyError( ITransportImpl transport, IContext context, Error error, IModelObject request)
  {
    if ( error == Error.heartbeatLost)
    {
      SLog.errorf( this, "Lost heartbeat on transport, %s", transport);
      transport.disconnect();
      return true;
    }

    return super.notifyError( transport, context, error, request);
  }

  private SocketAddress localAddress;
  private SocketAddress remoteAddress;
  private IRouter router;
  private int connectTimeout;
}
