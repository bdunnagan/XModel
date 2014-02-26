package org.xmodel.net.netty;

import java.net.InetSocketAddress;

import org.jboss.netty.buffer.ChannelBuffer;
import org.xmodel.future.AsyncFuture;
import org.xmodel.net.IXioChannel;
import org.xmodel.net.XioPeer;

public class NettyChannel implements IXioChannel
{

  @Override
  public XioPeer getPeer()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isConnected()
  {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void write( ChannelBuffer buffer)
  {
    // TODO Auto-generated method stub

  }

  @Override
  public InetSocketAddress getLocalAddress()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public InetSocketAddress getRemoteAddress()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public AsyncFuture<IXioChannel> getCloseFuture()
  {
    // TODO Auto-generated method stub
    return null;
  }

}
