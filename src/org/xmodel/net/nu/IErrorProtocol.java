package org.xmodel.net.nu;

import java.io.IOException;

/**
 * An interface for the error notification protocol.
 * <ul>
 *   <li>error</li>
 * </ul>
 */
public interface IErrorProtocol
{
  /**
   * Send an error notification.
   * @param sid The session id.
   * @param message The error message.
   */
  public void sendError( int sid, String message) throws IOException;
}
