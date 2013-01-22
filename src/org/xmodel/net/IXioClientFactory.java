package org.xmodel.net;

import java.net.InetSocketAddress;

/**
 * A factory interface for obtaining instances of XioClient.
 */
public interface IXioClientFactory
{
  /**
   * Returns a client that is connected to the specified address.
   * @param address The address.
   * @param connect True if the client should be connected before it is returned.
   * @return Returns a new XioClient.
   */
  public XioClient newInstance( InetSocketAddress address, boolean connect);
}
