package org.xmodel.net.nu;

import java.nio.ByteBuffer;

import org.xmodel.IModelObject;

/**
 * An interface for the common aspects of the messaging protocol such as header and message framing.
 */
public interface IProtocol
{
  public enum Type
  {
    sessionOpenRequest,
    sessionOpenResponse,
    sessionCloseRequest,
    error,
    attachRequest,
    attachResponse,
    detachRequest,
    syncRequest,
    syncResponse,
    addChild,
    removeChild,
    changeAttribute,
    clearAttribute,
    changeDirty,
    queryRequest,
    queryResponse,
    executeRequest,
    executeResponse,
    debugStepIn,
    debugStepOver,
    debugStepOut
  }

  /**
   * Initialize the specified ByteBuffer in preparation for the next message.
   * @param buffer The buffer.
   */
  public void initialize( ByteBuffer buffer);
  
  /**
   * Write the header and finalize the message content in the specified buffer. 
   * @param buffer The buffer.
   * @param type The message type.
   * @param sid The session id.
   * @param length The length of the message.
   */
  public void finalize( ByteBuffer buffer, int type, int sid, int length);
  
  /**
   * Deserialize an element from the specified buffer.
   * @param sid The session id.
   * @param buffer The buffer.
   * @return Returns the element.
   */
  public IModelObject readElement( int sid, ByteBuffer buffer);
  
  /**
   * Serialize the specified element into the specified buffer.
   * @param sid The session id.
   * @param buffer The buffer.
   * @param element The element.
   * @return Returns the number of serialized bytes.
   */
  public int writeElement( int sid, ByteBuffer buffer, IModelObject element);
}
