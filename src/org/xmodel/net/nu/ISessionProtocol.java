package org.xmodel.net.nu;

/**
 * An interface for the session protocol that implements the following message types:
 * <ul>
 *   <li>sessionBeginRequest</li>
 *   <li>sessionBeginResponse</li>
 *   <li>sessionEndRequest</li>
 * </ul>
 */
public interface ISessionProtocol
{
  /**
   * Send a request to begin a protocol session with the specified protocol version.
   * @param version The protocol version.
   */
  public void sendSessionBeginRequest( short version);
  
  /**
   * Send a response indicating that a session was successfully begun.
   * @param sid The session id that was allocated for the new session.
   */
  public void sendSessionBeginResponse( int sid);
  
  /**
   * Send a session end request.
   * @param sid The session id to be ended.
   */
  public void sendSessionEndRequest( int sid);
}
