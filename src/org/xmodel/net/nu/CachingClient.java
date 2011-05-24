package org.xmodel.net.nu;

import java.util.HashMap;
import java.util.Map;

import org.xmodel.external.IExternalReference;

/**
 * A class that implements the network caching policy protocol.
 */
public class CachingClient
{
  /**
   * Create a CachingClient to connect to the specified server.
   * @param host The server host.
   * @param port The server port.
   */
  public CachingClient( String host, int port)
  {
    map = new HashMap<IExternalReference, String>();
  }

  /**
   * Bind the specified XPath expression on behalf of the specified reference.
   * @param xpath The XPath expression.
   * @param reference The reference.
   */
  public void bind( String xpath, IExternalReference reference)
  {
  }

  /**
   * Unbind the specified XPath expression on behalf of the specified reference.
   * @param xpath The XPath expression.
   * @param reference The reference.
   */
  public void unbind( String xpath, IExternalReference reference)
  {
  }

  /**
   * Request that the server IExternalReference represented by the specified client IExternalReference be sync'ed.
   * @param reference The client IExternalReference.
   */
  public void sync( IExternalReference reference)
  {
    String key = map.get( reference);
    requestSync( key);
  }

  /**
   * Send a request to bind the specified xpath.
   * @param xpath The xpath.
   */
  private void requestBind( String xpath)
  {
  }
  
  /**
   * Send a request to unbind the specified xpath.
   * @param xpath The xpath.
   */
  private void requestUnbind( String xpath)
  {
  }
  
  /**
   * Send a request to sync the server IExternalReference with the specified key.
   * @param key The key.
   */
  private void requestSync( String key)
  {
  }
  
  /**
   * Wait for a bind request to complete.
   * @return Returns the
   */
  private String waitForBind()
  {
  }
  
  private Map<IExternalReference, String> map;
}
