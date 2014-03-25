package org.xmodel.net.connection;

public interface INetworkProtocol
{
  /**
   * Extract the correlation field from the specified message.
   * @param message The message.
   * @return Returns null or the correlation field.
   */
  public Object getCorrelation( Object message);
  
  /**
   * Returns the bytes of the message.
   * @param message The message.
   * @return Returns the bytes of the message.
   */
  public byte[] getBytes( Object message);
  
  /**
   * Create an echo request message.
   * @return Returns the message.
   */
  public Object createEchoRequest();
}
