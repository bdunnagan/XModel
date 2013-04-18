package org.xmodel.net;

import java.net.InetSocketAddress;

/**
 * A factory interface for obtaining instances of XioClient.
 */
public interface IXioClientFactory
{
  /**
   * Returns a client that is connected to the specified address, or is going to be connected to the specified address.
   * If the client is not connected, the caller must not connect the client to any other address but the one specified.
   * @param address The address.
   * @return Returns a new XioClient.
   */
  public XioClient newInstance( InetSocketAddress address);
}
