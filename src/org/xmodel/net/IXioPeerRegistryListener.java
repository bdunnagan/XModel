package org.xmodel.net;

public interface IXioPeerRegistryListener
{
  /**
   * Called when a new peer is registered.
   * @param name The name of the peer.
   * @param host The peer host.
   */
  public void onRegister( String name, String host); 
  
  /**
   * Called when a peer unregisters.
   * @param name The name.
   * @param host The peer host.
   */
  public void onUnregister( String name, String host);
}