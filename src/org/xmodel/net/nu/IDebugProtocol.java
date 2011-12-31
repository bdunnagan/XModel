package org.xmodel.net.nu;

import java.io.IOException;

/**
 * An interface for the protocol that supports remote XAction debugging including the following message types: 
 * <ul>
 *   <li>debugStepIn</li>
 *   <li>debugStepOver</li>
 *   <li>debugStepOut</li>
 * </ul>
 */
public interface IDebugProtocol
{
  /**
   * Send a request to perform a debug step.
   * @param sid The session id.
   */
  public void sendDebugStepIn( int sid) throws IOException;
  
  /**
   * Send a request to perform a debug step.
   * @param sid The session id.
   */
  public void sendDebugStepOut( int sid) throws IOException;
  
  /**
   * Send a request to perform a debug step.
   * @param sid The session id.
   */
  public void sendDebugStepOver( int sid) throws IOException;
}
