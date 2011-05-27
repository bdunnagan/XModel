package org.xmodel.net.nu;

import java.util.HashMap;
import java.util.Map;

import org.xmodel.IModelObject;
import org.xmodel.Xlate;
import org.xmodel.compress.TabularCompressor;
import org.xmodel.external.IExternalReference;

/**
 * A class that implements the network caching policy protocol.
 */
public class XPathClient extends Protocol
{
  /**
   * Create a CachingClient to connect to the specified server.
   * @param host The server host.
   * @param port The server port.
   */
  public XPathClient( String host, int port)
  {
    super( new TabularCompressor());
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

  /* (non-Javadoc)
   * @see org.xmodel.net.nu.Protocol#handleError(org.xmodel.net.nu.INetSender, java.lang.String)
   */
  @Override
  protected void handleError( INetSender sender, String message)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.nu.Protocol#handleAttachResponse(org.xmodel.net.nu.INetSender, java.lang.String, org.xmodel.IModelObject)
   */
  @Override
  protected void handleAttachResponse( INetSender sender, String xpath, IModelObject element)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.nu.Protocol#handleAddChild(org.xmodel.net.nu.INetSender, java.lang.String, org.xmodel.IModelObject, int)
   */
  @Override
  protected void handleAddChild( INetSender sender, String xpath, IModelObject child, int index)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.nu.Protocol#handleRemoveChild(org.xmodel.net.nu.INetSender, java.lang.String, int)
   */
  @Override
  protected void handleRemoveChild( INetSender sender, String xpath, int index)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.nu.Protocol#handleChangeAttribute(org.xmodel.net.nu.INetSender, java.lang.String, java.lang.String, byte[])
   */
  @Override
  protected void handleChangeAttribute( INetSender sender, String xpath, String attrName, byte[] attrValue)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.nu.Protocol#handleClearAttribute(org.xmodel.net.nu.INetSender, java.lang.String, java.lang.String)
   */
  @Override
  protected void handleClearAttribute( INetSender sender, String xpath, String attrName)
  {
  }

  private Map<String, IExternalReference> map;
  private INetSender sender;
  private INetReceiver receiver;
  private INetFramer framer;
}
