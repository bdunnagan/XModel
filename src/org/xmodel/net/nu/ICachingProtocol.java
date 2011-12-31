package org.xmodel.net.nu;

import java.io.IOException;

import org.xmodel.IModelObject;
import org.xmodel.external.IExternalReference;

/**
 * An interface for the protocol that supports the NetworkCachingPolicy including the message types:
 * <ul>
 *   <li>attachRequest</li>
 *   <li>attachResponse</li>
 *   <li>detachRequest</li>
 *   <li>syncRequest</li>
 *   <li>syncResponse</li>
 * </ul>
 */
public interface ICachingProtocol
{
  /**
   * Send a request to attach to the first element returned by the specified query.
   * @param sid The session id. 
   * @param query The query.
   */
  public void sendAttachRequest( int sid, String query) throws IOException;
  
  /**
   * Send a response to an attach request.
   * @param sid The session id.
   * @param element The query result (possibly null).
   */
  public void sendAttachResponse( int sid, IModelObject element) throws IOException;
  
  /**
   * Send a request to detach the currently attached element on the specified session.
   * @param sid The session id.
   */
  public void sendDetachRequest( int sid) throws IOException;
  
  /**
   * Send a request to retrieve the content referenced by the specified IExternalReference.
   * The content will be updated via the ITrackingProtocol interface.
   * @param reference The reference element.
   */
  public void sendSyncRequest( IExternalReference reference) throws IOException;
  
  /**
   * Send a response to a sync request indicating that the synchronization is complete.
   * @param sid The session id.
   */
  public void sendSyncResponse( int sid) throws IOException;
}
