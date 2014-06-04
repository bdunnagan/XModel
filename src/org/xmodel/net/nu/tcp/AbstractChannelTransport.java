package org.xmodel.net.nu.tcp;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import org.xmodel.IModelObject;
import org.xmodel.future.AsyncFuture;
import org.xmodel.future.FailureAsyncFuture;
import org.xmodel.net.nu.AbstractTransport;
import org.xmodel.net.nu.IConnectListener;
import org.xmodel.net.nu.IDisconnectListener;
import org.xmodel.net.nu.IEnvelopeProtocol;
import org.xmodel.net.nu.IReceiveListener;
import org.xmodel.net.nu.ITimeoutListener;
import org.xmodel.net.nu.ITransport;
import org.xmodel.net.nu.IWireProtocol;
import org.xmodel.xpath.expression.IContext;

public abstract class AbstractChannelTransport extends AbstractTransport
{
  public static final String notConnectedError = "Not connected";

  public AbstractChannelTransport( IWireProtocol wire, IEnvelopeProtocol envp, IContext transportContext, ScheduledExecutorService scheduler,
      List<IReceiveListener> receiveListeners, List<ITimeoutListener> timeoutListeners,
      List<IConnectListener> connectListeners, List<IDisconnectListener> disconnectListeners)
  {
    super( wire, envp, transportContext, scheduler, receiveListeners, timeoutListeners, connectListeners, disconnectListeners);
  }

  @Override
  public AsyncFuture<ITransport> sendImpl( IModelObject envelope) throws IOException
  {
    Channel channel = channelRef.get();
    if ( channel == null) return new FailureAsyncFuture<ITransport>( this, notConnectedError); 
    
    final AsyncFuture<ITransport> future = new AsyncFuture<ITransport>( this) {
      @Override 
      public void cancel()
      {
        Channel channel = channelRef.get();
        if ( channel != null) channel.close();
      }
    };
  
    // encode
    byte[] bytes = getWireProtocol().encode( envelope);
    
    // write
    ChannelFuture channelFuture = channel.writeAndFlush( Unpooled.wrappedBuffer( bytes));
    
    // future adapter
    channelFuture.addListener( new AsyncFutureAdapter<ITransport>( future));
    
    return future;
  }
  
  protected AtomicReference<Channel> channelRef;
}