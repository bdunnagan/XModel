package org.xmodel.net;

import java.io.IOException;

import org.xmodel.IDispatcher;
import org.xmodel.IModelObject;
import org.xmodel.ManualDispatcher;
import org.xmodel.external.CachingException;
import org.xmodel.external.ExternalReference;
import org.xmodel.external.ICachingPolicy;
import org.xmodel.external.IExternalReference;
import org.xmodel.log.Log;
import org.xmodel.net.stream.Connection;
import org.xmodel.net.stream.TcpClient;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.expression.Context;

/**
 * A class that implements the network caching policy protocol. 
 * This class is not thread-safe.
 */
public class Client extends Protocol
{
  /**
   * Create a CachingClient to connect to the specified server.
   * @param host The server host.
   * @param port The server port.
   * @param timeout The timeout for network operations in milliseconds.
   */
  public Client( String host, int port, int timeout) throws IOException
  {
    super( timeout);
    
    if ( client == null) 
    {
      client = new TcpClient();
      client.start();
    }
    
    this.host = host;
    this.port = port;
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.stream.ITcpListener#onConnect(org.xmodel.net.stream.Connection)
   */
  @Override
  public void onConnect( Connection connection)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.stream.ITcpListener#onClose(org.xmodel.net.stream.Connection)
   */
  @Override
  public void onClose( Connection connection)
  {
    super.onClose( connection);
    
    IExternalReference attached = (IExternalReference)map.get( connection).element;
    if ( attached != null)
    {
      IDispatcher dispatcher = dispatchers.peek();
      dispatcher.execute( new DisconnectEvent( attached));
      attached = null;
    }
  }

  /**
   * Connect (or reconnect) to the remote host.
   */
  public void connect( int timeout) throws IOException
  {
    if ( connection != null && connection.isOpen()) 
    {
      client.reconnect( connection, timeout);
    }
    else
    {
      connection = client.connect( host, port, timeout, this);
    }
  }

  /**
   * Disconnect form the remote host.
   */
  public void disconnect() throws IOException
  {
    if ( isConnected()) connection.close();
  }
  
  /**
   * @return Returns true if the client is connected.
   */
  public boolean isConnected()
  {
    return connection != null && connection.isOpen();
  }
  
  /**
   * Attach to the element on the specified xpath.
   * @param xpath The XPath expression.
   * @param reference The reference.
   */
  public void attach( String xpath, IExternalReference reference) throws IOException
  {
    connect( 300000);
    if ( connection != null && connection.isOpen())
    {
      sendVersion( connection, (byte)1);
      if ( dispatchers.empty()) pushDispatcher( reference.getModel().getDispatcher());
      
      ConnectionInfo info = new ConnectionInfo();
      info.xpath = xpath;
      info.element = reference;
      map.put( connection, info);
      
      sendAttachRequest( connection, xpath);
      
      info.listener = new Listener( connection, xpath, reference);
      info.listener.install( reference);
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
  public void detach( String xpath, IExternalReference reference) throws IOException
  {
    try
    {
      sendDetachRequest( connection, xpath);
    }
    finally
    {
      popDispatcher();
    }
  }

  /**
   * Send a debugStepIn message.
   */
  public void sendDebugStepIn() throws IOException
  {
    if ( connection == null) connection = client.connect( host, port, 30000, this);
    sendDebugStepIn( connection);
  }
  
  /**
   * Send a debugStepOver message.
   */
  public void sendDebugStepOver() throws IOException
  {
    if ( connection == null) connection = client.connect( host, port, 30000, this);
    sendDebugStepOver( connection);
  }
  
  /**
   * Send a debugStepOut message.
   */
  public void sendDebugStepOut() throws IOException
  {
    if ( connection == null) connection = client.connect( host, port, 30000, this);
    sendDebugStepOut( connection);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.nu.Protocol#handleError(org.xmodel.net.stream.Connection, java.lang.String)
   */
  @Override
  protected void handleError( Connection sender, String message)
  {
    log.error( message);
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.nu.Protocol#handleAttachResponse(org.xmodel.net.stream.Connection, org.xmodel.IModelObject)
   */
  @Override
  protected void handleAttachResponse( Connection sender, IModelObject element)
  {
    IExternalReference attached = (IExternalReference)map.get( connection).element;
    if ( attached != null)
    {
      ICachingPolicy cachingPolicy = attached.getCachingPolicy();
      cachingPolicy.update( attached, decode( sender, element));
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.Protocol#handleSyncResponse(org.xmodel.net.stream.Connection)
   */
  @Override
  protected void handleSyncResponse( Connection connection)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.Protocol#handleQueryResponse(org.xmodel.net.Connection, byte[])
   */
  @Override
  protected void handleQueryResponse( Connection sender, byte[] bytes)
  {
    // TODO: finish this
  }

  private final class DisconnectEvent implements Runnable
  {
    public DisconnectEvent( IExternalReference reference)
    {
      this.reference = reference;
    }
    
    public void run()
    {
      reference.setDirty( true);
    }
    
    private IExternalReference reference;
  }
  
  private static TcpClient client;
  
  private String host;
  private int port;
  private Connection connection;
  
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
