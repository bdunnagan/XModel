package org.xmodel.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import org.xmodel.net.stream.TcpServer;

/**
 * A class that implements the server-side of the network caching policy protocol.
 * This class is not thread-safe.
 */
public class Server extends Protocol
{
  public static int defaultPort = 27613;
  
  /**
   * Create a server bound to the specified local address and port.
   * @param host The local interface address.
   * @param port The local interface port.
   * @param debug True if debugging is enabled on this server.
   */
  public Server( String host, int port) throws IOException
  {
    this.pingTimeout = 30000;
    this.pings = new HashMap<ILink, Ping>();
    this.server = new TcpServer( host, port, this);
  }

  /**
   * Get server address.
   * @return Returns the server address.
   */
  public String getHost()
  {
    return server.getHost();
  }
  
  /**
   * Get server port.
   * @return Returns the server port.
   */
  public int getPort()
  {
    return server.getPort();
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
   * Start the server.
   * @param daemon True if the server thread should be a daemon.
   */
  public void start( boolean daemon) throws IOException
  {
    server.start( daemon);
  }
  
  /**
   * Stop the server.
   */
  public void stop()
  {
    server.stop();
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.Protocol#onReceive(org.xmodel.net.ILink, java.nio.ByteBuffer)
   */
  @Override
  public void onReceive( ILink link, ByteBuffer buffer)
  {
    Ping ping;
    
    synchronized( pings)
    {
      ping = pings.get( link);
      if ( ping == null)
      {
        ping = new Ping( this, link, pingTimeout);
        pings.put( link, ping);
      }
    }
    
    ping.onMessageReceived();
    
    super.onReceive( link, buffer);
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.Protocol#onClose(org.xmodel.net.ILink)
   */
  @Override
  public void onClose( ILink link)
  {
    Ping ping;
    synchronized( pings) { ping = pings.remove( link);}
    if ( ping != null) ping.stop();
    
    super.onClose( link);
  }

  private TcpServer server;
  private Map<ILink, Ping> pings;
  private int pingTimeout;
}
