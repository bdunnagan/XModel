package org.xmodel.net;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * An interface for an interprocess communication link used by classes in this package.
 */
public interface ILink
{
  /**
   * Send the specified bytes over the link.
   * @param bytes The bytes to send.
   */
  public void send( byte[] bytes) throws IOException;
  
  /**
   * Send the specified buffer over the link.
   * @param buffer The buffer.
   */
  public void send( ByteBuffer buffer) throws IOException;
  
  /**
   * @return Returns true if this link is open.
   */
  public boolean isOpen();
  
  /**
   * Close this link.
   */
  public void close();
  
  /**
   * An interface for receiving asynchronous link events.
   */
  public interface IListener
  {
    /**
     * Called when a link is closed.
     * @param link The link.
     */
    public void onClose( ILink link);
    
    /**
     * Called when data is read from a link.
     * @param link The link.
     * @param buffer The read buffer.
     */
    public void onReceive( ILink link, ByteBuffer buffer);
  }
}
