package org.xmodel.net;

import java.io.IOException;

import org.xmodel.IDispatcher;
import org.xmodel.external.IExternalReference;
import org.xmodel.net.stream.TcpClient;

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
   * @param daemon True if client thread should be a daemon thread.
   */
  public Client( String host, int port, int timeout, boolean daemon) throws IOException
  {
    super( timeout);
    
    if ( client == null) 
    {
      client = new TcpClient();
      client.start( daemon);
    }
    
    this.host = host;
    this.port = port;
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.Protocol#onClose(org.xmodel.net.IConnection)
   */
  @Override
  public void onClose( ILink link)
  {
    super.onClose( link);
    
    IExternalReference attached = (IExternalReference)map.get( link).element;
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
    if ( link == null || !link.isOpen()) 
    {
      link = client.connect( host, port, timeout, this);
    }
  }

  /**
   * Disconnect form the remote host.
   */
  public void disconnect() throws IOException
  {
    if ( isConnected()) link.close();
  }
  
  /**
   * @return Returns true if the client is connected.
   */
  public boolean isConnected()
  {
    return link != null && link.isOpen();
  }
  
  /**
   * Attach to the element on the specified xpath.
   * @param xpath The XPath expression.
   * @param reference The reference.
   */
  public void attach( String xpath, IExternalReference reference) throws IOException
  {
    connect( 300000);
    attach( link, xpath, reference);
  }

  /**
   * Detach from the element on the specified path.
   * @param xpath The XPath expression.
   * @param reference The reference.
   */
  public void detach( String xpath, IExternalReference reference) throws IOException
  {
    detach( link, xpath, reference);
  }

  /**
   * Send a debugStepIn message.
   */
  public void sendDebugStepIn() throws IOException
  {
    connect( 300000);
    sendDebugStepIn( link);
  }
  
  /**
   * Send a debugStepOver message.
   */
  public void sendDebugStepOver() throws IOException
  {
    connect( 300000);
    sendDebugStepOver( link);
  }
  
  /**
   * Send a debugStepOut message.
   */
  public void sendDebugStepOut() throws IOException
  {
    connect( 300000);
    sendDebugStepOut( link);
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
  private ILink link;
}
