package org.xmodel.net.nu;

import java.nio.ByteBuffer;

/**
 * An interface for receiving messages.
 */
public interface IRecipient
{
  /**
   * Called when the specified connection is connected.
   * @param connection The connection.
   */
  public void connected( Connection connection);
  
  /**
   * Called when the specified connection is lost.
   * @param connection The connection.
   * @param nice True if the connection was terminated gracefully.
   */
  public void disconnected( Connection connection, boolean nice);
  
  /**
   * Called when data is received from a connection.
   * @param connection The connection.
   * @param buffer The buffer.
   * @param Returns the number of bytes consumed from the buffer.
   */
  public int received( Connection connection, ByteBuffer buffer);
}
