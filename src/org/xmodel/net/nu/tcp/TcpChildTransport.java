package org.xmodel.net.nu.tcp;

import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import org.xmodel.future.AsyncFuture;
import org.xmodel.future.SuccessAsyncFuture;
import org.xmodel.net.nu.ITransport;
import org.xmodel.net.nu.protocol.Protocol;
import org.xmodel.xpath.expression.IContext;

public class TcpChildTransport extends AbstractChannelTransport
{
  public TcpChildTransport( Protocol protocol, IContext transportContext, ScheduledExecutorService scheduler, SocketChannel channel)
  {
    super( protocol, transportContext, scheduler); 
    
    channelRef = new AtomicReference<Channel>();
    channelRef.set( channel);
  }
  
  @Override
  public AsyncFuture<ITransport> connect( int timeout)
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
}
