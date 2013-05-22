package org.xmodel.net;

import java.util.Iterator;

import org.xmodel.future.AsyncFuture;

/**
 * Interface for registering and querying peer connections by name.
 */
public interface IXioPeerRegistry
{
  /**
   * Register the specified peer with the specified name.
   * @param peer The peer.
   * @param name A name, not necessarily unique, to associate with the peer.
   */
  public void register( XioPeer peer, String name);
  
  /**
   * Cancel a peer registration by name.
   * @param peer The peer.
   * @param name The name associated with the peer.
   */
  public void unregister( XioPeer peer, String name);
  
  /**
   * Unregister all names associated with the specified peer.
   * @param peer The peer.
   */
  public void unregisterAll( XioPeer peer);

  /**
   * Returns a future for a peer registration with the specified name.
   * @param name The name associated with the peer.
   * @return Returns a future for a peer registration with the specified name.
   */
  public AsyncFuture<XioPeer> getRegistrationFuture( String name);
  
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
}
