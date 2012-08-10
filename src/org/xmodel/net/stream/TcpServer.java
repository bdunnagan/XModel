package org.xmodel.net.stream;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;

import org.xmodel.log.Log;
import org.xmodel.net.ILink;

/**
 * A java.nio based Server.
 */
public final class TcpServer extends TcpBase
{
  /**
   * Create a server bound to the specified host and port.
   * @param host The host address.
   * @param port The port.
   * @param listener The listener for socket events.
   */
  public TcpServer( String host, int port, ILink.IListener listener) throws IOException
  {
    super( listener);
    address = new InetSocketAddress( host, port);    
  }
  
  /**
   * Get server address.
   * @return Returns the server address.
   */
  public String getHost()
  {
    return address.getHostName();
  }
  
  /**
   * Get server port.
   * @return Returns the server port.
   */
  public int getPort()
  {
    return address.getPort();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.stream.TcpBase#useSSL(org.xmodel.net.stream.SSL)
   */
  @Override
  public synchronized final void useSSL( SSL ssl)
  {
    ssl.getSSLEngine().setUseClientMode( false);
    super.useSSL( ssl);
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.stream.TcpBase#start(boolean)
   */
  @Override
  public void start( boolean daemon) throws IOException
  {
    log.debugf( "Server listening at %s:%d.", address.getAddress().getHostAddress(), address.getPort());
    
    serverChannel = ServerSocketChannel.open();
    serverChannel.configureBlocking( false);
    serverChannel.socket().bind( address);
    
    // register accept selector
    serverChannel.register( selector, SelectionKey.OP_ACCEPT);
    
    // start the server thread
    super.start( daemon);
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.stream.TcpManager#stop()
   */
  @Override
  public void stop()
  {
    super.stop();
    
    try
    {
      if ( serverChannel != null) serverChannel.close();
    }
    catch( IOException e)
    {
      log.exception( e);
    }
    
    serverChannel = null;
  }

  private final static Log log = Log.getLog( TcpServer.class);

  private InetSocketAddress address;
  private ServerSocketChannel serverChannel;
}
