package org.xmodel.net.nu;

import java.nio.ByteBuffer;

/**
 * An interface for receiving data from a network peer.
 */
public interface INetReceiver
{
  /**
   * Called when a message is received from a peer.
   * @param sender The sender for the peer.
   * @param buffer The buffer.
   */
  public void receive( INetSender sender, ByteBuffer buffer);
}
