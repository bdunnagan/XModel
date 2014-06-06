package org.xmodel.net.nu.tcp;

import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import java.io.IOException;
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

public class TcpChildTransport extends AbstractChannelTransport
{
  public TcpChildTransport( Protocol protocol, IContext transportContext, ScheduledExecutorService scheduler, SocketChannel channel,
      List<IConnectListener> connectListeners, List<IDisconnectListener> disconnectListeners,
      List<IReceiveListener> receiveListeners, List<IErrorListener> errorListeners) 
  {
    super( protocol, transportContext, scheduler, connectListeners, disconnectListeners, receiveListeners, errorListeners); 
    
    channelRef = new AtomicReference<Channel>();
    channelRef.set( channel);
  }
  
  @Override
  public AsyncFuture<ITransport> connect( int timeout) throws IOException
  {
    return new SuccessAsyncFuture<ITransport>( this);
  }

  @Override
  public AsyncFuture<ITransport> disconnect() throws IOException
  {
    Channel channel = channelRef.get();
    if ( channel != null) channel.close();
    
    AsyncFuture<ITransport> future = new AsyncFuture<ITransport>( this);
    channel.closeFuture().addListener( new AsyncFutureAdapter<ITransport>( future));
    return future;    
  }
}
