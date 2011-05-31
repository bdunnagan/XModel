package org.xmodel.net;

import java.io.IOException;

import org.xmodel.IDispatcher;
import org.xmodel.IModelObject;
import org.xmodel.ManualDispatcher;
import org.xmodel.Xlate;
import org.xmodel.external.CachingException;
import org.xmodel.external.ExternalReference;
import org.xmodel.external.ICachingPolicy;
import org.xmodel.external.IExternalReference;
import org.xmodel.log.Log;
import org.xmodel.net.stream.Connection;
import org.xmodel.net.stream.TcpClient;
import org.xmodel.net.stream.Connection.IListener;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.Context;
import org.xmodel.xpath.expression.IExpression;

/**
 * A class that implements the network caching policy protocol.
 */
public class XPathClient extends Protocol implements IListener
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
    
    client = new TcpClient( this, this, this);
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.stream.Connection.IListener#connected(org.xmodel.net.stream.Connection)
   */
  @Override
  public void connected( Connection connection)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.stream.Connection.IListener#disconnected(org.xmodel.net.stream.Connection)
   */
  @Override
  public void disconnected( Connection connection)
  {
    exit = true;
    if ( attached != null)
    {
      dispatcher.execute( new DisconnectEvent( attached));
      attached = null;
    }
  }

  /**
   * Attach to the element on the specified xpath.
   * @param xpath The XPath expression.
   * @param reference The reference.
   */
  public void attach( String xpath, IExternalReference reference) throws IOException
  {
    if ( connection != null && connection.isOpen()) 
    {
      client.reconnect( connection);
    }
    else
    {
      connection = client.connect( host, port);
    }
    
    client.process();
    if ( connection.isOpen())
    {
      sendVersion( connection, (byte)1);
      dispatcher = reference.getModel().getDispatcher();
      attached = reference;
      sendAttachRequest( connection, xpath);
    }
    else
    {
      throw new CachingException( "Unable to connect to server.");
    }
  }

  /**
   * Detach from the element on the specified path.
   * @param xpath The XPath expression.
   * @param reference The reference.
   */
  public void detach( String xpath, IExternalReference reference)
  {
    sendDetachRequest( connection, xpath);
  }

  /**
   * Request that the server IExternalReference represented by the specified client IExternalReference be sync'ed.
   * @param reference The client IExternalReference.
   */
  public void sync( IExternalReference reference)
  {
    String key = Xlate.get( reference, "net:key", (String)null);
    sendSyncRequest( connection, key);
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
    
    thread = new Thread( clientRunnable, "client");
    thread.setDaemon( true);
    thread.start();
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.Protocol#handleAddChild(org.xmodel.net.INetSender, java.lang.String, byte[], int)
   */
  @Override
  protected void handleAddChild( INetSender sender, String xpath, byte[] child, int index)
  {
    dispatcher.execute( new AddChildEvent( xpath, child, index));
  }

  /**
   * Process an add child event in the appropriate thread.
   * @param xpath The xpath of the parent.
   * @param bytes The child that was added.
   * @param index The index of insertion.
   */
  private void processAddChild( String xpath, byte[] bytes, int index)
  {
    if ( attached == null) return;
    
    IExpression parentExpr = XPath.createExpression( xpath);
    if ( parentExpr != null)
    {
      IModelObject parent = parentExpr.queryFirst( attached);
      IModelObject child = compressor.decompress( bytes, 0);
      if ( parent != null) parent.addChild( child, index);
    }
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.nu.Protocol#handleRemoveChild(org.xmodel.net.nu.INetSender, java.lang.String, int)
   */
  @Override
  protected void handleRemoveChild( INetSender sender, String xpath, int index)
  {
    dispatcher.execute( new RemoveChildEvent( xpath, index));
  }

  /**
   * Process an remove child event in the appropriate thread.
   * @param xpath The xpath of the parent.
   * @param index The index of insertion.
   */
  private void processRemoveChild( String xpath, int index)
  {
    if ( attached == null) return;
    
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
    dispatcher.execute( new ChangeAttributeEvent( xpath, attrName, attrValue));
  }

  /**
   * Process an change attribute event in the appropriate thread.
   * @param xpath The xpath of the element.
   * @param attrName The name of the attribute.
   * @param attrValue The new value.
   */
  private void processChangeAttribute( String xpath, String attrName, Object attrValue)
  {
    if ( attached == null) return;
    
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
    dispatcher.execute( new ClearAttributeEvent( xpath, attrName));
  }

  /**
   * Process a clear attribute event in the appropriate thread.
   * @param xpath The xpath of the element.
   * @param attrName The name of the attribute.
   */
  private void processClearAttribute( String xpath, String attrName)
  {
    if ( attached == null) return;
    
    IExpression elementExpr = XPath.createExpression( xpath);
    if ( elementExpr != null)
    {
      IModelObject element = elementExpr.queryFirst( attached);
      if ( element != null) element.removeAttribute( attrName);
    }
  }
  
  private final class AddChildEvent implements Runnable
  {
    public AddChildEvent( String xpath, byte[] child, int index)
    {
      this.xpath = xpath;
      this.child = child;
      this.index = index;
    }
    
    public void run()
    {
      processAddChild( xpath, child, index);
    }
    
    private String xpath;
    private byte[] child;
    private int index;
  }
  
  private final class RemoveChildEvent implements Runnable
  {
    public RemoveChildEvent( String xpath, int index)
    {
      this.xpath = xpath;
      this.index = index;
    }
    
    public void run()
    {
      processRemoveChild( xpath, index);
    }
    
    private String xpath;
    private int index;
  }
  
  private final class ChangeAttributeEvent implements Runnable
  {
    public ChangeAttributeEvent( String xpath, String attrName, Object attrValue)
    {
      this.xpath = xpath;
      this.attrName = attrName;
      this.attrValue = attrValue;
    }
    
    public void run()
    {
      processChangeAttribute( xpath, attrName, attrValue);
    }
    
    private String xpath;
    private String attrName;
    private Object attrValue;
  }
  
  private final class ClearAttributeEvent implements Runnable
  {
    public ClearAttributeEvent( String xpath, String attrName)
    {
      this.xpath = xpath;
      this.attrName = attrName;
    }
    
    public void run()
    {
      processClearAttribute( xpath, attrName);
    }
    
    private String xpath;
    private String attrName;
  }
  
  private final class DisconnectEvent implements Runnable
  {
    public DisconnectEvent( IExternalReference attached)
    {
      this.attached = attached;
    }
    
    public void run()
    {
      System.out.println( "mark dirty");
      attached.setDirty( true);
      System.out.println( "resync");
      attached.getChildren();
    }
    
    private IExternalReference attached;
  }
  
  private final Runnable clientRunnable = new Runnable() {
    public void run()
    {
      exit = false;
      while( !exit)
      {
        try
        {
          client.process( 500);
        }
        catch( IOException e)
        {
          break;
        }
      }
    }
  };
  
  private String host;
  private int port;
  private TcpClient client;
  private Connection connection;
  private IExternalReference attached;
  private Thread thread;
  private boolean exit;
  private IDispatcher dispatcher;
  
  private static Log log = Log.getLog(  "org.xmodel.net");
  
  public static void main( String[] args) throws Exception
  {
    String xml = "<config host=\"127.0.0.1\" port=\"27613\" xpath=\".\"/>";
    XmlIO xmlIO = new XmlIO();
    IModelObject config = xmlIO.read( xml);
    
    IExternalReference reference = new ExternalReference( "parent");
    NetworkCachingPolicy cachingPolicy = new NetworkCachingPolicy();
    cachingPolicy.configure( new Context( config), config);
    reference.setCachingPolicy( cachingPolicy);
    reference.setDirty( true);

    ManualDispatcher dispatcher = new ManualDispatcher();
    reference.getModel().setDispatcher( dispatcher);
    
    reference.getChildren();
    
    while( true)
    {
      System.out.println( xmlIO.write( reference));
      dispatcher.process();
      Thread.sleep( 1000);
    }
  }
}
