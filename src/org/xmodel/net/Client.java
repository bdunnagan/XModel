package org.xmodel.net;

import java.io.IOException;
import org.xmodel.log.SLog;
import org.xmodel.net.stream.SSL;
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
    
    synchronized( Client.class)
    {
      if ( client == null)
      {
        client = new TcpClient();
        client.start( daemon);
      }
    }
    
    this.host = host;
    this.port = port;
    this.retries = 1;
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
   * Use SSL according to the specified SSL instance (call prior to connecting client).
   * @param ssl The SSL instance.
   */
  public final void useSSL( SSL ssl)
  {
    client.useSSL( ssl);
  }
  
  /**
   * Connect (or reconnect) to the remote host.
   * @return Returns a connected session or null on timeout.
   */
  public Session connect( int timeout) throws IOException
  {
    return connect( timeout, retries);
  }
  
  /**
   * Connect (or reconnect) to the remote host.
   * @param timeout The connection timeout.
   * @param retry The number of retries.
   * @return Returns a connected session or null on timeout.
   */
  public Session connect( int timeout, int retry) throws IOException
  {
    if ( link == null || !link.isOpen())
    {
      SLog.debugf( this, "Opening connection to %s:%d, timeout=%d", host, port, timeout);
      link = client.connect( host, port, timeout, this);
    }
    
    if ( link != null)
    {
      try
      {
        SLog.debugf( this, "Opening session on %s:%d", host, port);
        Session session = openSession( link);
        
        SLog.debugf( this, "Opened new session, %X", session.getID());
        return session;
      }
      catch( IOException e1)
      {
        SLog.warnf( this, "Open session failed, closing connection, will try again: %s", e1.getMessage());
        link.close();
        link = null;
      }
    }

    if ( retry > 0) return connect( timeout, retry-1);
    
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
  private int retries;
  private ILink link;
}
