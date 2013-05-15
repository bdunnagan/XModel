package org.xmodel.net;

import java.net.InetSocketAddress;

import org.jboss.netty.channel.Channel;
import org.xmodel.future.AsyncFuture;
import org.xmodel.future.AsyncFuture.IListener;
import org.xmodel.future.FailureAsyncFuture;
import org.xmodel.log.SLog;

public class XioServerPeer extends XioPeer
{
  public XioServerPeer()
  {
    super();
  }

  public XioServerPeer( Channel channel)
  {
    setChannel( channel);
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.XioPeer#reconnect()
   */
  @Override
  protected AsyncFuture<XioPeer> reconnect()
  {
    if ( server == null) return new FailureAsyncFuture<XioPeer>( this, "Channel cannot be reconnected.");
    
    final AsyncFuture<XioPeer> reconnectFuture = new AsyncFuture<XioPeer>( this) {
      public void cancel()
      {
        throw new UnsupportedOperationException();
      }
    };
    
    AsyncFuture<XioPeer> future = server.getPeerByHost( address.getAddress().getHostAddress());
    future.addListener( new IListener<XioPeer>() {
      public void notifyComplete( AsyncFuture<XioPeer> future) throws Exception
      {
        if ( future.isSuccess())
        {
          IXioPeerRegistryListener listener = new IXioPeerRegistryListener() {
            public void onRegister( String name, String host)
            {
              server.removePeerRegistryListener( this);
              
              try
              {
                setChannel( server.getPeerByHost( host).getInitiator().getChannel());        
                reconnectFuture.notifySuccess();
              }
              catch( Exception e)
              {
                SLog.exception( this, e);
              }
            }
            public void onUnregister( String name, String host)
            {
            }
          };
          
          server.addPeerRegistryListener( listener);
        }
      }
    });
    
    return reconnectFuture;
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.XioPeer#setChannel(org.jboss.netty.channel.Channel)
   */
  @Override
  protected synchronized void setChannel( Channel channel)
  {
    Object attachment = channel.getAttachment();
    if ( attachment instanceof XioServerPeer)
    {
      XioServerPeer peer = (XioServerPeer)attachment;
      this.address = peer.address;
      this.name = peer.name;
    }
    
    super.setChannel( channel);
    
    server = (XioServer)channel.getParent().getAttachment();
  }

  /**
   * @return Returns the remote address to which this client is, or was last, connected.
   */
  public synchronized InetSocketAddress getRemoteAddress()
  {
    if ( address != null) return address;
    Channel channel = getChannel();
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

  private XioServer server;
  private InetSocketAddress address;
  private String name;
}
