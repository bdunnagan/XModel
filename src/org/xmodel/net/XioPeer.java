package org.xmodel.net;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.SucceededChannelFuture;
import org.xmodel.IModelObject;
import org.xmodel.external.IExternalReference;
import org.xmodel.future.AsyncFuture;
import org.xmodel.future.AsyncFuture.IListener;
import org.xmodel.xpath.expression.IContext;

/**
 * This class represents an XIO protocol end-point.
 * (thread-safe)
 */
public class XioPeer
{
  protected XioPeer()
  {
  }
  
  protected XioPeer( Channel channel)
  {
    this.channel = channel;
    this.channel.setAttachment( this);
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
            Channel channel = future.getInitiator().getChannel();
            setChannel( channel);
            XioChannelHandler handler = (XioChannelHandler)channel.getPipeline().get( "xio");
            handler.getRegisterProtocol().registerRequestProtocol.send( channel, name);
          }
        }
      });
    }
    else
    {
      if ( channel == null) throw new IllegalStateException( "Peer is not connected.");
      XioChannelHandler handler = (XioChannelHandler)channel.getPipeline().get( "xio");
      handler.getRegisterProtocol().registerRequestProtocol.send( channel, name);
    }
  }
  
  /**
   * Unregister this peer under the specified name with the remote endpoint.
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
            Channel channel = future.getInitiator().getChannel();
            setChannel( channel);
            XioChannelHandler handler = (XioChannelHandler)channel.getPipeline().get( "xio");
            handler.getRegisterProtocol().unregisterRequestProtocol.send( channel, name);
          }
        }
      });
    }
    else
    {
      if ( channel == null) throw new IllegalStateException( "Peer is not connected.");
      XioChannelHandler handler = (XioChannelHandler)channel.getPipeline().get( "xio");
      handler.getRegisterProtocol().unregisterRequestProtocol.send( channel, name);
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
      setChannel( future.getInitiator().getChannel());
    }
    else
    {
      if ( channel == null) throw new IllegalStateException( "Peer is not connected.");
      XioChannelHandler handler = (XioChannelHandler)channel.getPipeline().get( "xio");
      handler.getBindProtocol().bindRequestProtocol.send( reference, channel, readonly, query, timeout);
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
            Channel channel = future.getInitiator().getChannel();
            setChannel( channel);
            XioChannelHandler handler = (XioChannelHandler)channel.getPipeline().get( "xio");
            handler.getBindProtocol().unbindRequestProtocol.send( channel, netID);
          }
        }
      });
    }
    else
    {
      if ( channel == null) throw new IllegalStateException( "Peer is not connected.");
      XioChannelHandler handler = (XioChannelHandler)channel.getPipeline().get( "xio");
      handler.getBindProtocol().unbindRequestProtocol.send( channel, netID);
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
      setChannel( future.getInitiator().getChannel());
    }
    
    if ( channel == null) throw new IllegalStateException( "Peer is not connected.");
    XioChannelHandler handler = (XioChannelHandler)channel.getPipeline().get( "xio");
    return handler.getBindProtocol().syncRequestProtocol.send( channel, netID, timeout);
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
      setChannel( future.getInitiator().getChannel());
    }
    
    if ( channel == null) throw new IllegalStateException( "Peer is not connected.");
    XioChannelHandler handler = (XioChannelHandler)channel.getPipeline().get( "xio");
    return handler.getExecuteProtocol().requestProtocol.send( channel, context, vars, element, timeout);
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
    Channel channel = getChannel();
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
            Channel channel = future.getInitiator().getChannel();
            setChannel( channel);
            XioChannelHandler handler = (XioChannelHandler)channel.getPipeline().get( "xio");
            handler.getExecuteProtocol().requestProtocol.send( channel, correlation, context, vars, element, callback, timeout);
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
      XioChannelHandler handler = (XioChannelHandler)channel.getPipeline().get( "xio");
      handler.getExecuteProtocol().requestProtocol.send( channel, correlation, context, vars, element, callback, timeout);
    }
  }
  
  /**
   * Cancel the request with the specified correlation number.
   * @param correlation The correlation number.
   */
  public void cancel( int correlation)
  {
    XioChannelHandler handler = (XioChannelHandler)channel.getPipeline().get( "xio");
    handler.getExecuteProtocol().requestProtocol.cancel( channel, correlation);
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
  protected synchronized void setChannel( Channel channel)
  {
    this.channel = channel;
    getRemoteAddress();
  }
  
  /**
   * @return Returns null or the underlying channel.
   */
  protected synchronized Channel getChannel()
  {
    return channel;
  }
  
  /**
   * @return Returns the remote address to which this client is, or was last, connected.
   */
  public synchronized InetSocketAddress getRemoteAddress()
  {
    return (channel != null)? (InetSocketAddress)channel.getRemoteAddress(): null;
  }
  
  /**
   * Set the registered peer of this peer.
   * @param name The name.
   */
  public synchronized void setRegisteredName( String name)
  {
  }
  
  /**
   * @return Returns null or the registered name of this peer.
   */
  public synchronized String getRegisteredName()
  {
    return null;
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
  public ChannelFuture close()
  {
    return isConnected()? channel.close(): new SucceededChannelFuture( channel);
  }

  private Channel channel;
  private boolean reconnect;
}
