package org.xmodel.net.nu;

import java.nio.ByteBuffer;

/**
 * An interface for sending data to a network peer.
 */
public interface INetSender
{
  /**
   * Send the contents of the specified buffer.
   * @param buffer The buffer to send.
   * @return Returns true if the message was sent.
   */
  public boolean send( ByteBuffer buffer);
    
  /**
   * Send the contents of the specified buffer and wait to receive a response.
   * @param buffer The buffer.
   * @param timeout The amount of time to wait in milliseconds.
   * @return Returns null or the buffer containing the response.
   */
  public ByteBuffer send( ByteBuffer buffer, int timeout);
  
  /**
   * Close this sender permanently.
   */
  public void close();
}
