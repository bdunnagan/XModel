package org.xmodel.net;

/**
 * A factory interface for obtaining instances of XioClient.
 */
public interface IXioClientFactory
{
  /**
   * Returns a client that is connected to the specified address and port.
   * @param address
   * @param port
   * @return
   */
  public XioClient getClient( String address, int port);
}
