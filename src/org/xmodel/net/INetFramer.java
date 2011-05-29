package org.xmodel.net;

import java.nio.ByteBuffer;

/**
 * An interface for framing messages in a buffer. Implementations of this interface
 * should do the least amount of work required to find the length of the next message.
 * The protocol should be defined so that this work can be performed efficiently since
 * this method will be invoked each time data is received from the network and may be
 * invoked many times before a complete message is received.
 */
public interface INetFramer
{
  /**
   * Returns the number of bytes in the next message in the buffer.
   * @param buffer The buffer.
   * @return Returns -1 or the number of bytes in the message.
   */
  public int frame( ByteBuffer buffer);
}
