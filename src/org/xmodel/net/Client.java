package org.xmodel.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.xmodel.log.SLog;
import org.xmodel.net.stream.TcpClient;

/**
 * A class that implements the network caching policy protocol. 
 */
public class Client extends Protocol
{
  /**
   * Create a CachingClient to connect to the specified server.
   * @param host The server host.
   * @param port The server port.
   * @param daemon True if client thread should be a daemon thread.
   */
  public Client( String host, int port, boolean daemon) throws IOException
  {
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
    this.pingTimeout = 30000;
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
   * Set the time to wait for a ping response before closing the connection.
   * @param timeout The timeout in milliseconds.
   */
  public void setPingTimeout( int timeout)
  {
    pingTimeout = timeout;
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
      if ( link != null) ping = new Ping( this, link, pingTimeout);
    }
    
    if ( link != null)
    {
      try
      {
        return openSession( link);
      }
      catch( IOException e)
      {
        SLog.exception( this, e);
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
      ping.stop();
      ping = null;
      
      link.close();
      link = null;
    }
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.Protocol#onReceive(org.xmodel.net.ILink, java.nio.ByteBuffer)
   */
  @Override
  public void onReceive( ILink link, ByteBuffer buffer)
  {
    if ( ping != null) ping.onMessageReceived();
    super.onReceive( link, buffer);
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
  private volatile Ping ping;
  private int pingTimeout;
}
