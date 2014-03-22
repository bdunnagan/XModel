package org.xmodel.net.connection;

public interface INetworkMessageFactory
{
  /**
   * Create a new message from the specified message bytes.
   * @param bytes The message bytes.
   * @return Returns the new message object.
   */
  public INetworkMessage newMessage( byte[] bytes);
  
  /**
   * @return Returns a new echo request message.
   */
  public INetworkMessage newEchoRequest();
}
