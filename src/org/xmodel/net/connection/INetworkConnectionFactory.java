package org.xmodel.net.connection;

public interface INetworkConnectionFactory
{
  /**
   * Create a new, unconnected connection object.
   * @return Returns the new connection.
   */
  public INetworkConnection newConnection();
}
