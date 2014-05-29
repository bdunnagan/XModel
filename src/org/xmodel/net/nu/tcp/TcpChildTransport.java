package org.xmodel.net.nu.tcp;

import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import java.io.IOException;
import org.xmodel.future.AsyncFuture;
import org.xmodel.net.nu.IContextManager;
import org.xmodel.net.nu.IProtocol;
import org.xmodel.net.nu.ITransport;

public class TcpChildTransport extends AbstractChannelTransport
{
  public TcpChildTransport( IProtocol protocol, IContextManager contexts, SocketChannel channel)
  {
    super( protocol, contexts);
    channelRef.set( channel);
  }
  
  @Override
  public AsyncFuture<ITransport> connect( int timeout) throws IOException
  {
    throw new UnsupportedOperationException();
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
