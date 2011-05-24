package org.xmodel.net.nu;

import java.util.HashMap;
import java.util.Map;

import org.xmodel.Xlate;
import org.xmodel.external.IExternalReference;

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

  /**
   * Send a request to bind the specified xpath.
   * @param xpath The xpath.
   */
  private void sendAttach( String xpath)
  {
  }
  
  /**
   * Send a request to unbind the specified xpath.
   * @param xpath The xpath.
   */
  private void sendDetach( String xpath)
  {
  }
  
  /**
   * Send a request to sync the server IExternalReference on the specified xpath.
   * @param xpath The xpath.
   */
  private void sendSync( String xpath)
  {
  }
    
  private Map<String, IExternalReference> map;
}
