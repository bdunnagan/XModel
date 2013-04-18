package org.xmodel.net;

import java.util.Iterator;
import org.jboss.netty.channel.Channel;
import org.xmodel.future.AsyncFuture;

/**
 * Interface for registering and querying peer connections by name.
 */
public interface IXioPeerRegistry
{
  /**
   * Register the specified remote-host with the specified name.
   * @param name A name, not necessarily unique, to associate with the peer.
   * @param host The host to be registered.
   */
  public void register( String name, String host);
  
  /**
   * Cancel a peer registration by name and host.
   * @param name The name associated with the peer.
   * @param host The remote host.
   */
  public void unregister( String name, String host);
  
  /**
   * Returns a future for a peer connection from the specified host.
   * @param host The remote host.
   * @return Returns a future for a peer connection from the specified host.
   */
  public AsyncFuture<XioPeer> lookupByHost( String host);
  
  /**
   * Returns an iterator over XioPeer instances registered under the specified name.
   * @param name The name.
   * @return Returns the associated peers.
   */
  public Iterator<XioPeer> lookupByName( String name);
  
  /**
   * Add a listener for peer registration.
   * @param listener The The listener.
   */
  public void addListener( IXioPeerRegistryListener listener);
  
  /**
   * Remove a listener.
   * @param listener The listener.
   */
  public void removeListener( IXioPeerRegistryListener listener);
  
  /**
   * Called when a channel is connected.
   * @param channel The channel.
   */
  public void channelConnected( Channel channel);
  
  /**
   * Called when a channel is disconnected.
   * @param channel The channel.
   */
  public void channelDisconnected( Channel channel);
}
