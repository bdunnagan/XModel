package org.xmodel.net.stream;

import java.nio.ByteBuffer;

public interface ITcpListener
{
  /**
   * Called when a connection is established.
   * @param connection The connection.
   */
  public void onConnect( Connection connection);
  
  /**
   * Called when a connection is closed.
   * @param connection The connection.
   */
  public void onClose( Connection connection);
  
  /**
   * Called when data is read from a connection.
   * @param connection The connection.
   * @param buffer The read buffer.
   */
  public void onReceive( Connection connection, ByteBuffer buffer);
}
