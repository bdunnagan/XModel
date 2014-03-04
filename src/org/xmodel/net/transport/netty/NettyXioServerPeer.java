package org.xmodel.net.transport.netty;

import java.net.InetSocketAddress;
import org.xmodel.future.AsyncFuture;
import org.xmodel.future.AsyncFuture.IListener;
import org.xmodel.future.FailureAsyncFuture;
import org.xmodel.log.SLog;
import org.xmodel.net.IXioChannel;
import org.xmodel.net.IXioPeerRegistry;
import org.xmodel.net.XioPeer;

public class NettyXioServerPeer extends XioPeer
{
  public NettyXioServerPeer( IXioChannel channel, IXioPeerRegistry registry)
  {
    super( channel, registry);
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.XioPeer#reconnect()
   */
  @Override
  protected AsyncFuture<XioPeer> reconnect()
  {
    String name = getRegisteredName();
    if ( name == null) return new FailureAsyncFuture<XioPeer>( this, "Channel cannot be reconnected - name is null.");
    
    final AsyncFuture<XioPeer> reconnectFuture = new AsyncFuture<XioPeer>( this) {
      public void cancel()
      {
        throw new UnsupportedOperationException();
      }
    };

    AsyncFuture<XioPeer> future = getPeerRegistry().getRegistrationFuture( name);
    future.addListener( new IListener<XioPeer>() {
      public void notifyComplete( AsyncFuture<XioPeer> future) throws Exception
      {
        if ( future.isSuccess())
        {
          try
          {
            setChannel( future.getInitiator().getChannel());        
            reconnectFuture.notifySuccess();
          }
          catch( Exception e)
          {
            SLog.exception( this, e);
          }
        }
      }
    });
    
    return reconnectFuture;
  }

  /**
   * @return Returns the remote address to which this client is, or was last, connected.
   */
  public synchronized InetSocketAddress getRemoteAddress()
  {
    if ( address != null) return address;
    IXioChannel channel = getChannel();
    address = (channel != null)? (InetSocketAddress)channel.getRemoteAddress(): null;
    return address;
  }
  
  /**
   * Set the registered peer of this peer.
   * @param name The name.
   */
  public synchronized void setRegisteredName( String name)
  {
    this.name = name;
  }
  
  /**
   * @return Returns null or the registered name of this peer.
   */
  public synchronized String getRegisteredName()
  {
    return name;
  }

  private InetSocketAddress address;
  private String name;
}
