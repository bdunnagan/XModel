package org.xmodel.net.nu.tcp;

import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.GenericFutureListener;
import org.xmodel.future.AsyncFuture;

public class AsyncFutureAdapter<T> implements GenericFutureListener<ChannelFuture>
{
  public AsyncFutureAdapter( AsyncFuture<T> future)
  {
    this.future = future;
  }
  
  @Override
  public void operationComplete( ChannelFuture channelFuture) throws Exception
  {
    if ( channelFuture.isSuccess())
    {
      future.notifySuccess();
    }
    else
    {
      future.notifyFailure( channelFuture.cause());
    }
  }
  
  protected AsyncFuture<T> future;
}
