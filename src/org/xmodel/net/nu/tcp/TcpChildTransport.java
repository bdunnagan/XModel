package org.xmodel.net.nu.tcp;

import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;
import org.xmodel.future.AsyncFuture;
import org.xmodel.future.SuccessAsyncFuture;
import org.xmodel.net.nu.IRouter;
import org.xmodel.net.nu.ITransport;
import org.xmodel.net.nu.protocol.Protocol;
import org.xmodel.xpath.expression.IContext;

public class TcpChildTransport extends AbstractChannelTransport implements IRouter
{
  public TcpChildTransport( TcpServerRouter router, Protocol protocol, IContext transportContext, SocketChannel channel)
  {
    super( protocol, transportContext); 
    
    channelRef = new AtomicReference<Channel>();
    channelRef.set( channel);
    
    this.router = router;
  }
  
  @Override
  public void setConnectTimeout( int timeout)
  {
  }

  @Override
  public AsyncFuture<ITransport> connect()
  {
    return new SuccessAsyncFuture<ITransport>( this);
  }

  @Override
  public AsyncFuture<ITransport> disconnect()
  {
    Channel channel = channelRef.get();
    if ( channel != null) channel.close();
    
    AsyncFuture<ITransport> future = new AsyncFuture<ITransport>( this);
    channel.closeFuture().addListener( new AsyncFutureAdapter<ITransport>( future));
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
  public Iterator<ITransport> resolve( String route)
  {
    return router.resolve( route);
  }

  private IRouter router;
}
