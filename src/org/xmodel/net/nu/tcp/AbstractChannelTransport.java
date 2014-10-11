package org.xmodel.net.nu.tcp;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import org.xmodel.IModelObject;
import org.xmodel.future.AsyncFuture;
import org.xmodel.future.FailureAsyncFuture;
import org.xmodel.net.nu.AbstractTransport;
import org.xmodel.net.nu.ITransport;
import org.xmodel.net.nu.protocol.Protocol;
import org.xmodel.xpath.expression.IContext;

public abstract class AbstractChannelTransport extends AbstractTransport
{
  public static final String notConnectedError = "Not connected";

  public AbstractChannelTransport( Protocol protocol, IContext transportContext)
  {
    super( protocol, transportContext);
  }

  @Override
  public AsyncFuture<ITransport> sendImpl( IModelObject envelope, IModelObject request)
  {
    Channel channel = channelRef.get();
    if ( channel == null) 
    {
      getEventPipe().notifyError( this, getTransportContext(), ITransport.Error.sendFailed, envelope);
      return new FailureAsyncFuture<ITransport>( this, notConnectedError);
    }
    
    final AsyncFuture<ITransport> future = new AsyncFuture<ITransport>( this) {
      @Override 
      public void cancel()
      {
        throw new UnsupportedOperationException();
      }
    };
    
    // encode
    byte[] bytes = encode( envelope, future);
    if ( bytes != null)
    {
      // write
      ChannelFuture channelFuture = channel.writeAndFlush( Unpooled.wrappedBuffer( bytes));
      
      // future adapter
      channelFuture.addListener( new AsyncFutureAdapter<ITransport>( future));
    }
    
    return future;
  }
  
  private byte[] encode( IModelObject envelope, AsyncFuture<ITransport> future)
  {
    try
    {
      return getProtocol().wire().encode( envelope);
    }
    catch( IOException e)
    {
      future.notifyFailure( e);
      getEventPipe().notifyError( this, getTransportContext(), ITransport.Error.encodeFailed, null);
      return null;
    }
  }
  
  protected AtomicReference<Channel> channelRef;
}