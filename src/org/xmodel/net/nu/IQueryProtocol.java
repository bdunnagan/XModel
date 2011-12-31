package org.xmodel.net.nu;

import java.io.IOException;

import org.xmodel.IModelObject;

/**
 * An interface for the protocol that supports remote querying including the message types:
 * <ul>
 *   <li>queryRequest</li>
 *   <li>queryResponse</li>
 * </ul>
 */
public interface IQueryProtocol
{
  /**
   * Send a query request.
   * @param sid The session id.
   * @param query The query.
   * @param timeout The timeout in milliseconds.
   */
  public void sendQueryRequest( int sid, String query, int timeout) throws IOException;
  
  /**
   * Send a response to a query request.
   * @param sid The session id.
   * @param element The query response element.
   */
  public void sendQueryResponse( int sid, IModelObject element) throws IOException;
}
