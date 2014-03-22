package org.xmodel.net.connection;

public interface INetworkMessage
{
  /**
   * @return Returns the bytes comprising the content of the message.
   */
  public byte[] getBytes();
  
  /**
   * @return Returns null or the correlation object for this message.
   */
  public Object getCorrelation();
}
