package org.xmodel.net.nu;

/**
 * This class provides an interface for the client-side of the protocol.
 */
public class Client extends Peer
{
  /**
   * Connect to the specified server.
   * @param host The server host.
   * @param port The server port.
   * @param timeout The timeout in milliseconds.
   * @return Returns true if the connection was established.
   */
  public boolean connect( String host, int port, int timeout) throws InterruptedException
  {
  }
  
  /**
   * Close the connection that was previously established with a call to the <code>connect</code> method.
   */
  public void close()
  {
  }
}
