package org.xmodel.net.connection;

import org.jboss.netty.buffer.ChannelBuffer;

public interface INetworkMessage
{
  /**
   * @return Returns null or the correlation key.
   */
  public Object getCorrelation();
  
  /**
   * @return Returns 0 or the expiration timestamp of the message in GMT milliseconds.
   */
  public long getExpiration();
  
  /**
   * @return Returns the content of the message as a byte array.
   */
  public byte[] getBytes();
  
  /**
   * Set the content of this message to the specified buffer.
   * @param buffer The buffer containing the content of the message.
   */
  public void setChannelBuffer( ChannelBuffer buffer);
  
  /**
   * @return Returns the content of the messages as a ChannelBuffer.
   */
  public ChannelBuffer getChannelBuffer();
  
  /**
   * Set the response to this request message.
   * @param message The message.
   */
  public void setResponse( INetworkMessage message);
  
  /**
   * @return Returns null or the response to this request message.
   */
  public INetworkMessage getResponse();
}
