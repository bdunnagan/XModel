package org.xmodel.net.protocol;

import org.jboss.netty.buffer.ChannelBuffer;

public class Protocol
{
  /**
   * Message type field (must be less than 32).
   */
  public enum Type
  {
    pingRequest,
    pingResponse,
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
    executeResponse
  }
  
  public static Type getMessageType( ChannelBuffer buffer)
  {
  }
}
