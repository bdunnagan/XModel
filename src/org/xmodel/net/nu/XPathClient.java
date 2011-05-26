package org.xmodel.net.nu;

import java.util.HashMap;
import java.util.Map;

import org.xmodel.Xlate;
import org.xmodel.external.IExternalReference;
import org.xmodel.net.nu.stream.TcpClient;

/**
 * A class that implements the network caching policy protocol.
 */
public class XPathClient
{
  /**
   * Create a CachingClient to connect to the specified server.
   * @param host The server host.
   * @param port The server port.
   */
  public XPathClient( String host, int port)
  {
    map = new HashMap<String, IExternalReference>();
  }

  /**
   * Attach to the element on the specified xpath.
   * @param xpath The XPath expression.
   * @param reference The reference.
   */
  public void attach( String xpath, IExternalReference reference)
  {
    sendAttach( xpath);
    // wait for response
    // update reference
  }

  /**
   * Detach from the element on the specified path.
   * @param xpath The XPath expression.
   * @param reference The reference.
   */
  public void detach( String xpath, IExternalReference reference)
  {
    sendDetach( xpath);
  }

  /**
   * Request that the server IExternalReference represented by the specified client IExternalReference be sync'ed.
   * @param reference The client IExternalReference.
   */
  public void sync( IExternalReference reference)
  {
    String key = Xlate.get( reference, "net:key", (String)null);
    sendSync( key);
  }

  private Map<String, IExternalReference> map;
  private INetSender sender;
  private INetReceiver receiver;
  private INetFramer framer;
}
