package org.xmodel.net.nu;

import java.io.IOException;

import org.xmodel.IModelObject;

/**
 * An interface for the protocol that supports remote XAction invocation including the message types:
 * <ul>
 *   <li>executeRequest</li>
 *   <li>executeResponse</li>
 * </ul>
 */
public interface IExecuteProtocol
{
  /**
   * Send a request to execute the specified script and optionally wait for a response.
   * @param sid The session id.
   * @param script The script to be executed remotely.
   * @param timeout The timeout in milliseconds.
   */
  public void sendExecuteRequest( int sid, IModelObject script, int timeout) throws IOException;
  
  /**
   * Send a response to an execute request.
   * @param sid The session id.
   * @param element The response element.
   */
  public void sendExecuteResponse( int sid, IModelObject element) throws IOException;
}
