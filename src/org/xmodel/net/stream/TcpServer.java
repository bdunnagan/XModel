package org.xmodel.net.stream;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;

import org.xmodel.log.Log;

/**
 * A java.nio based Server.
 */
public final class TcpServer extends TcpManager
{
  /**
   * Create a server bound to the specified host and port.
   * @param host The host address.
   * @param port The port.
   * @param listener The listener for socket events.
   */
  public TcpServer( String host, int port, ITcpListener listener) throws IOException
  {
    super( listener);
    
    serverChannel = ServerSocketChannel.open();
    serverChannel.configureBlocking( false);
    serverChannel.socket().bind( new InetSocketAddress( host, port));
    
    // register accept selector
    serverChannel.register( selector, SelectionKey.OP_ACCEPT);
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

  private final static Log log = Log.getLog( "org.xmodel.net.stream");
  
  private ServerSocketChannel serverChannel;
}
