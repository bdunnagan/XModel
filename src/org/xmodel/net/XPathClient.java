package org.xmodel.net;

import java.io.IOException;

import org.xmodel.IModelObject;
import org.xmodel.Xlate;
import org.xmodel.external.ICachingPolicy;
import org.xmodel.external.IExternalReference;
import org.xmodel.log.Log;
import org.xmodel.net.stream.Connection;
import org.xmodel.net.stream.TcpClient;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.IExpression;

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
    this.host = host;
    this.port = port;
    
    client = new TcpClient( this, this);
  }

  /**
   * Attach to the element on the specified xpath.
   * @param xpath The XPath expression.
   * @param reference The reference.
   */
  public void attach( String xpath, IExternalReference reference) throws IOException
  {
    if ( connection.isOpen()) 
    {
      client.reconnect( connection);
    }
    else
    {
      connection = client.connect( host, port);
    }
    
    attached = reference;
    sendAttachRequest( sender, xpath);
  }

  /**
   * Detach from the element on the specified path.
   * @param xpath The XPath expression.
   * @param reference The reference.
   */
  public void detach( String xpath, IExternalReference reference)
  {
    sendDetachRequest( sender, xpath);
  }

  /**
   * Request that the server IExternalReference represented by the specified client IExternalReference be sync'ed.
   * @param reference The client IExternalReference.
   */
  public void sync( IExternalReference reference)
  {
    String key = Xlate.get( reference, "net:key", (String)null);
    sendSyncRequest( sender, key);
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.nu.Protocol#handleError(org.xmodel.net.nu.INetSender, java.lang.String)
   */
  @Override
  protected void handleError( INetSender sender, String message)
  {
    log.error( message);
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.nu.Protocol#handleAttachResponse(org.xmodel.net.nu.INetSender, org.xmodel.IModelObject)
   */
  @Override
  protected void handleAttachResponse( INetSender sender, IModelObject element)
  {
    if ( attached != null)
    {
      ICachingPolicy cachingPolicy = attached.getCachingPolicy();
      cachingPolicy.update( attached, element);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.nu.Protocol#handleAddChild(org.xmodel.net.nu.INetSender, java.lang.String, org.xmodel.IModelObject, int)
   */
  @Override
  protected void handleAddChild( INetSender sender, String xpath, IModelObject child, int index)
  {
    IExpression parentExpr = XPath.createExpression( xpath);
    if ( parentExpr != null)
    {
      IModelObject parent = parentExpr.queryFirst( attached);
      if ( parent != null) parent.addChild( child, index);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.nu.Protocol#handleRemoveChild(org.xmodel.net.nu.INetSender, java.lang.String, int)
   */
  @Override
  protected void handleRemoveChild( INetSender sender, String xpath, int index)
  {
    IExpression parentExpr = XPath.createExpression( xpath);
    if ( parentExpr != null)
    {
      IModelObject parent = parentExpr.queryFirst( attached);
      if ( parent != null) parent.removeChild( index);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.nu.Protocol#handleChangeAttribute(org.xmodel.net.nu.INetSender, java.lang.String, java.lang.String, java.lang.Object)
   */
  @Override
  protected void handleChangeAttribute( INetSender sender, String xpath, String attrName, Object attrValue)
  {
    IExpression elementExpr = XPath.createExpression( xpath);
    if ( elementExpr != null)
    {
      IModelObject element = elementExpr.queryFirst( attached);
      if ( element != null) element.setAttribute( attrName, attrValue);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.nu.Protocol#handleClearAttribute(org.xmodel.net.nu.INetSender, java.lang.String, java.lang.String)
   */
  @Override
  protected void handleClearAttribute( INetSender sender, String xpath, String attrName)
  {
    IExpression elementExpr = XPath.createExpression( xpath);
    if ( elementExpr != null)
    {
      IModelObject element = elementExpr.queryFirst( attached);
      if ( element != null) element.removeAttribute( attrName);
    }
  }

  private String host;
  private int port;
  private TcpClient client;
  private Connection connection;
  private INetSender sender;
  private IExternalReference attached;
  
  private static Log log = Log.getLog(  "org.xmodel.net");
}
