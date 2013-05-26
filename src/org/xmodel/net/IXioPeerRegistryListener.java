package org.xmodel.net;

public interface IXioPeerRegistryListener
{
  /**
   * Called when a new peer is registered.
   * @param peer The peer.
   * @param name The name of the peer.
   */
  public void onRegister( XioPeer peer, String name); 
  
  /**
   * Called when a peer unregisters.
   * @param peer The peer.
   * @param name The name.
   */
  public void onUnregister( XioPeer peer, String name);
}