package org.xmodel.net.nu.stream;

import java.nio.ByteBuffer;

/**
 * An interface for receiving messages.
 */
public interface IReceiver
{
  /**
   * Called when data is received from a connection.
   * @param connection The connection.
   * @param buffer The buffer.
   */
  public void received( Connection connection, ByteBuffer buffer);
}
