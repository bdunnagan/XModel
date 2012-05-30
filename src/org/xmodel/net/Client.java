package org.xmodel.net;

import java.io.IOException;

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

  /**
   * @return Returns the remote host.
   */
  public String getHost()
  {
    return host;
  }
  
  /**
   * @return Returns the remote port.
   */
  public int getPort()
  {
    return port;
  }
  
  /**
   * Connect (or reconnect) to the remote host.
   * @return Returns a connected session or null on timeout.
   */
  public Session connect( int timeout) throws IOException
  {
    if ( link == null || !link.isOpen())
    {
      link = client.connect( host, port, timeout, this);
    }
    
    if ( link != null)
    {
      Session session = openSession( link);
      return session;
    }
    
    return null;
  }

  /**
   * Disconnect form the remote host.
   */
  public void disconnect() throws IOException
  {
    if ( isConnected()) 
    {
      link.close();
      link = null;
    }
  }
  
  /**
   * @return Returns true if the client is connected.
   */
  public boolean isConnected()
  {
    return link != null && link.isOpen();
  }
    
  private static TcpClient client;
  private String host;
  private int port;
  private ILink link;
}
