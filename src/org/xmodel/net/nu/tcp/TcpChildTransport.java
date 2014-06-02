package org.xmodel.net.nu.tcp;

import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;

import org.xmodel.future.AsyncFuture;
import org.xmodel.future.SuccessAsyncFuture;
import org.xmodel.net.nu.IProtocol;
import org.xmodel.net.nu.ITransport;
import org.xmodel.xpath.expression.IContext;

public class TcpChildTransport extends AbstractChannelTransport
{
  public TcpChildTransport( IProtocol protocol, IContext transportContext, ScheduledExecutorService scheduler, SocketChannel channel)
  {
    super( protocol, transportContext, scheduler);
    channelRef.set( channel);
  }
  
  @Override
  public AsyncFuture<ITransport> connect( int timeout) throws IOException
  {
    notifyConnect();
    return new SuccessAsyncFuture<ITransport>( this);
  }

  @Override
  public AsyncFuture<ITransport> disconnect() throws IOException
  {
    notifyDisconnect();
    
    Channel channel = channelRef.get();
    if ( channel != null) channel.close();
    
    AsyncFuture<ITransport> future = new AsyncFuture<ITransport>( this);
    channel.closeFuture().addListener( new AsyncFutureAdapter<ITransport>( future));
    return future;    
  }
}
