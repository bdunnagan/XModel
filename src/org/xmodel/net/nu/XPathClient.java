package org.xmodel.net.nu;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmodel.Xlate;
import org.xmodel.external.IExternalReference;

/**
 * A class that implements the network caching policy protocol.
 */
public class XPathClient implements IRecipient
{
  /**
   * Create a CachingClient to connect to the specified server.
   * @param host The server host.
   * @param port The server port.
   */
  public XPathClient( String host, int port)
  {
    map = new HashMap<String, IExternalReference>();
    client = new Client( this);
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
   * Send the protocol version requested.
   * @param version The version.
   */
  private void sendVersion( int version)
  {
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
    
  /* (non-Javadoc)
   * @see org.xmodel.net.nu.IRecipient#connected(org.xmodel.net.nu.Connection)
   */
  @Override
  public void connected( Connection connection)
  {
    sendVersion( 0xF0000001);
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.nu.IRecipient#disconnected(org.xmodel.net.nu.Connection, boolean)
   */
  @Override
  public void disconnected( Connection connection, boolean nice)
  {
    for( IExternalReference reference: map.values())
    {
      reference.setDirty( false);
      
      List<String> attributes = Arrays.asList( reference.getCachingPolicy().getStaticAttributes());
      for( String attrName: reference.getAttributeNames())
      {
        if ( !attributes.contains( attrName))
          reference.removeAttribute( attrName);
      }
      reference.removeChildren();
      
      reference.setDirty( true);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.nu.IRecipient#received(org.xmodel.net.nu.Connection, java.nio.ByteBuffer)
   */
  @Override
  public int received( Connection connection, ByteBuffer buffer)
  {
    return 0;
  }

  private Map<String, IExternalReference> map;
  private Client client;
}
