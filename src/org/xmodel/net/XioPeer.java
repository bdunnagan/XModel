package org.xmodel.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;
import org.xmodel.IModelObject;
import org.xmodel.external.IExternalReference;
import org.xmodel.future.AsyncFuture;
import org.xmodel.future.AsyncFuture.IListener;
import org.xmodel.future.SuccessAsyncFuture;
import org.xmodel.xpath.expression.IContext;

/**
 * This class represents an XIO protocol end-point.
 * (thread-safe)
 */
public class XioPeer
{
  protected XioPeer()
  {
    this( null, null);
  }
  
  protected XioPeer( IXioChannel channel)
  {
    this( channel, null);
  }
  
  protected XioPeer( IXioChannel channel, IXioPeerRegistry registry)
  {
    this.channel = channel;
    this.registry = registry;
  }

  /**
   * Specify whether the peer should attempt to reconnect if a message is sent and the underlying channel
   * is no longer connected.  Only one retry attempt will be made.
   * @param reconnect True if connection should be retried.
   */
  public void setReconnect( boolean reconnect)
  {
    this.reconnect = reconnect;
  }
  
  /**
   * Send a heartbeat.
   */
  public void heartbeat() throws IOException
  {
    if ( channel == null) throw new IllegalStateException( "Peer is not connected.");
    channel.getEchoProtocol().requestProtocol.send( channel);
  }
  
  /**
   * Register this peer under the specified name with the remote endpoint.
   * @param name The name to be associated with this peer.
   */
  public void register( final String name) throws IOException, InterruptedException
  {
    if ( reconnect && (channel == null || !channel.isConnected()))
    {
      AsyncFuture<XioPeer> future = reconnect();
      if ( future == null) throw new IllegalStateException( "Peer is not connected.");
      
      future.addListener( new IListener<XioPeer>() {
        public void notifyComplete( AsyncFuture<XioPeer> future) throws Exception
        {
          if ( future.isSuccess())
          {
            IXioChannel channel = future.getInitiator().getChannel();
            channel.getRegisterProtocol().registerRequestProtocol.send( channel, name);
          }
        }
      });
    }
    else
    {
      if ( channel == null) throw new IllegalStateException( "Peer is not connected.");
      channel.getRegisterProtocol().registerRequestProtocol.send( channel, name);
    }
  }
  
  /**
   * Unregister all the names of this peer with the remote endpoint.
   * @param name The name previously associated with this peer.
   */
  public void unregisterAll() throws IOException, InterruptedException
  {
    if ( reconnect && (channel == null || !channel.isConnected()))
    {
      AsyncFuture<XioPeer> future = reconnect();
      if ( future == null) throw new IllegalStateException( "Peer is not connected.");
      
      future.addListener( new IListener<XioPeer>() {
        public void notifyComplete( AsyncFuture<XioPeer> future) throws Exception
        {
          if ( future.isSuccess())
          {
            IXioChannel channel = future.getInitiator().getChannel();
            channel.getRegisterProtocol().unregisterRequestProtocol.send( channel);
          }
        }
      });
    }
    else
    {
      if ( channel == null) throw new IllegalStateException( "Peer is not connected.");
      channel.getRegisterProtocol().unregisterRequestProtocol.send( channel);
    }
  }
  
  /**
   * Unregister the specified name of this peer with the remote endpoint.
   * @param name The name previously associated with this peer.
   */
  public void unregister( final String name) throws IOException, InterruptedException
  {
    if ( reconnect && (channel == null || !channel.isConnected()))
    {
      AsyncFuture<XioPeer> future = reconnect();
      if ( future == null) throw new IllegalStateException( "Peer is not connected.");
      
      future.addListener( new IListener<XioPeer>() {
        public void notifyComplete( AsyncFuture<XioPeer> future) throws Exception
        {
          if ( future.isSuccess())
          {
            IXioChannel channel = future.getInitiator().getChannel();
            channel.getRegisterProtocol().unregisterRequestProtocol.send( channel, name);
          }
        }
      });
    }
    else
    {
      if ( channel == null) throw new IllegalStateException( "Peer is not connected.");
      channel.getRegisterProtocol().unregisterRequestProtocol.send( channel, name);
    }
  }
  
  /**
   * Remotely bind the specified query.
   * @param reference The reference for which the bind is being performed.
   * @param readonly True if binding is readonly.
   * @param query The query to bind on the remote peer.
   * @param timeout The timeout in milliseconds to wait.
   */
  public void bind( final IExternalReference reference, final boolean readonly, final String query, final int timeout) throws InterruptedException
  {
    if ( reconnect && (channel == null || !channel.isConnected()))
    {
      AsyncFuture<XioPeer> future = reconnect();
      if ( future == null || !future.await( timeout)) throw new IllegalStateException( "Peer is not connected.");
    }
    else
    {
      if ( channel == null) throw new IllegalStateException( "Peer is not connected.");
      channel.getBindProtocol().bindRequestProtocol.send( reference, channel, readonly, query, timeout);
    }
  }
  
  /**
   * Unbind the query that returned the specified network identifier.
   * @param netID The network identifier of the query root element.
   */
  public void unbind( final int netID) throws InterruptedException
  {
    if ( reconnect && (channel == null || !channel.isConnected()))
    {
      AsyncFuture<XioPeer> future = reconnect();
      if ( future == null) throw new IllegalStateException( "Peer is not connected.");
      
      future.addListener( new IListener<XioPeer>() {
        public void notifyComplete( AsyncFuture<XioPeer> future) throws Exception
        {
          if ( future.isSuccess())
          {
            IXioChannel channel = future.getInitiator().getChannel();
            channel.getBindProtocol().unbindRequestProtocol.send( channel, netID);
          }
        }
      });
    }
    else
    {
      if ( channel == null) throw new IllegalStateException( "Peer is not connected.");
      channel.getBindProtocol().unbindRequestProtocol.send( channel, netID);
    }
  }
  
  /**
   * Sync the remote element with the specified network identifier.
   * @param netID The network identifier of the remote element.
   * @param timeout The timeout in milliseconds.
   * @return Returns null or the sync'ed remote element.
   */
  public IModelObject sync( int netID, int timeout) throws InterruptedException
  {
    if ( reconnect && (channel == null || !channel.isConnected()))
    {
      AsyncFuture<XioPeer> future = reconnect();
      if ( future == null || !future.await( timeout)) throw new IllegalStateException( "Peer is not connected.");
    }
    
    if ( channel == null) throw new IllegalStateException( "Peer is not connected.");
    return channel.getBindProtocol().syncRequestProtocol.send( channel, netID, timeout);
  }
  
  /**
   * Remotely execute the specified operation synchronously.
   * @param context The local context.
   * @param vars Shared variables from the local context.
   * @param element The element representing the operation to execute.
   * @param timeout The timeout in milliseconds.
   * @return Returns the result.
   */
  public Object[] execute( IContext context, String[] vars, IModelObject element, int timeout) throws XioExecutionException, IOException, InterruptedException
  {
    if ( reconnect && (channel == null || !channel.isConnected()))
    {
      AsyncFuture<XioPeer> future = reconnect();
      if ( future == null || !future.await( timeout)) throw new IllegalStateException( "Peer is not connected.");
    }
    
    if ( channel == null) throw new IllegalStateException( "Peer is not connected.");
    return channel.getExecuteProtocol().requestProtocol.send( channel, context, vars, element, timeout);
  }
  
  /**
   * Remotely execute the specified operation asynchronously.
   * @param context The local context.
   * @param correlation The correlation number.
   * @param vars Shared variables from the local context.
   * @param element The element representing the operation to execute.
   * @param callback The callback.
   * @param timeout The timeout in milliseconds.
   */
  public void execute( final IContext context, 
                       final int correlation, 
                       final String[] vars, 
                       final IModelObject element, 
                       final IXioCallback callback, 
                       final int timeout) throws IOException, InterruptedException
  {
    IXioChannel channel = getChannel();
    if ( reconnect && (channel == null || !channel.isConnected()))
    {
      // TODO: timeout needed here for reconnect
      AsyncFuture<XioPeer> future = reconnect();
      if ( future == null) throw new IllegalStateException( "Peer is not connected.");
      
      future.addListener( new IListener<XioPeer>() {
        public void notifyComplete( AsyncFuture<XioPeer> future) throws Exception
        {
          if ( future.isSuccess())
          {
            IXioChannel channel = future.getInitiator().getChannel();
            channel.getExecuteProtocol().requestProtocol.send( channel, correlation, context, vars, element, callback, timeout);
          }
          else
          {
            context.getExecutor().execute( new Runnable() {
              public void run()
              {
                callback.onError( context, "Peer is not connected.");
                callback.onComplete( context);
              }
            });
          }
        }
      });
    }
    else
    {
      if ( channel == null) throw new IllegalStateException( "Peer is not connected.");
      channel.getExecuteProtocol().requestProtocol.send( channel, correlation, context, vars, element, callback, timeout);
    }
  }
  
  /**
   * Cancel the request with the specified correlation number.
   * @param correlation The correlation number.
   */
  public void cancel( int correlation)
  {
    channel.getExecuteProtocol().requestProtocol.cancel( channel, correlation);
  }
  
  /**
   * This method is called when a message is sent but the underlying channel is closed.  
   * Sub-classes may re-establish the connection.
   * @return Returns null or the ChannelFuture for the connection.
   */
  protected AsyncFuture<XioPeer> reconnect()
  {
    return null;
  }
  
  /**
   * Set the underlying channel.
   * @param channel The channel.
   */
  public synchronized void setChannel( IXioChannel channel)
  {
    this.channel = channel;
    getRemoteAddress();
  }
  
  /**
   * @return Returns null or the underlying channel.
   */
  public synchronized IXioChannel getChannel()
  {
    return channel;
  }
  
  /**
   * @return Returns the remote address to which this client is, or was last, connected.
   */
  public synchronized InetSocketAddress getLocalAddress()
  {
    return (channel != null)? (InetSocketAddress)channel.getLocalAddress(): null;
  }
  
  /**
   * @return Returns the remote address to which this client is, or was last, connected.
   */
  public synchronized InetSocketAddress getRemoteAddress()
  {
    return (channel != null)? (InetSocketAddress)channel.getRemoteAddress(): null;
  }
  
  /**
   * @return Returns true if the connection to the server is established.
   */
  public synchronized boolean isConnected()
  {
    return (channel != null)? channel.isConnected(): false;
  }
  
  /**
   * Close the connection.
   */
  public AsyncFuture<IXioChannel> close()
  {
    return isConnected()? channel.close(): new SuccessAsyncFuture<IXioChannel>( channel);
  }

  /**
   * @return Returns null or the peer registry.
   */
  public IXioPeerRegistry getPeerRegistry()
  {
    return registry;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals( Object object)
  {
    IXioChannel otherChannel = ((XioPeer)object).getChannel();
    if ( channel == null || otherChannel == null) return false;
    return channel == otherChannel;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode()
  {
    if ( channel == null) return 0;
    return channel.hashCode();
  }

  public static Timer timer = new HashedWheelTimer();
  
  private IXioChannel channel;
  private IXioPeerRegistry registry;
  private boolean reconnect;
}
