package org.xmodel.net.nu;

import java.io.IOException;

import org.xmodel.IModelObject;

/**
 * An interface for the protocol that supports the NetworkCachingPolicy including the message types:
 * <ul>
 *   <li>addChild</li>
 *   <li>removeChild</li>
 *   <li>changeAttribute</li>
 *   <li>clearAttribute</li>
 *   <li>changeDirty</li>
 * </ul>
 */
public interface ITrackingProtocol
{
  /**
   * Send a notification that a child was added in an attached tree.
   * @param sid The session id.
   * @param path The path of the parent element.
   * @param element The element that was added.
   * @param index The insertion index.
   */
  public void sendAddChild( int sid, String path, IModelObject element, int index) throws IOException;
  
  /**
   * Send a notification that a child was removed in an attached tree.
   * @param sid The session id.
   * @param path The path of the parent element.
   * @param index The index of the element that was removed.
   */
  public void sendRemoveChild( int sid, String path, int index) throws IOException;

  /**
   * Send a notification that an attribute was changed in an attached tree.
   * @param sid The session id.
   * @param path The path of the element.
   * @param attribute The name of the attribute.
   * @param value The new value of the attribute.
   */
  public void sendChangeAttribute( int sid, String path, String attribute, Object value) throws IOException;
  
  /**
   * Send a notification that the dirty state of a reference element has changed.
   * @param sid The session id.
   * @param path The path of the element.
   * @param dirty The dirty state.
   */
  public void sendChangeDirty( int sid, String path, boolean dirty) throws IOException;
  
  /**
   * Send a notification that an attribute has been cleared.
   * @param sid The session id.
   * @param path The path of the element.
   * @param attribute The name of the attribute.
   */
  public void sendClearAttribute( int sid, String path, String attribute) throws IOException;
}
