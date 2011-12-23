package org.xmodel.net;

import java.io.IOException;

import org.xmodel.ImmediateDispatcher;
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
   */
  public Server( String host, int port, int timeout) throws IOException
  {
    super( timeout);
    pushDispatcher( new ImmediateDispatcher());
    server = new TcpServer( host, port, this);
  }

  /**
   * Start the server.
   * @param daemon True if the server thread should be a daemon.
   */
  public void start( boolean daemon)
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
   * @see org.xmodel.net.Protocol#onClose(org.xmodel.net.IConnection)
   */
  @Override
  public void onClose( ILink link)
  {
    super.onClose( link);
    
    ConnectionInfo info = map.get( link);
    if ( info != null) doDetach( link, info.xpath);
    
    map.remove( link);
  }

  private TcpServer server;
}
