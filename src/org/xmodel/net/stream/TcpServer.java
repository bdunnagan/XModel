package org.xmodel.net.stream;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;

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
    this.listener = listener;
    
    InetSocketAddress address = new InetSocketAddress( host, port);
    serverChannel = ServerSocketChannel.open();
    serverChannel.configureBlocking( false);
    serverChannel.socket().bind( address);
    
    // register accept selector
    serverChannel.register( selector, SelectionKey.OP_ACCEPT);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.stream.TcpManager#process(int)
   */
  protected boolean process( int timeout) throws IOException
  {
    // wait for connection
    if ( selector.select( timeout) == 0) return false; 
      
    // handle events
    Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
    while( iter.hasNext())
    {
      SelectionKey readyKey = iter.next();
      iter.remove();
      
      if ( !readyKey.isValid()) continue;
      
      try
      {
        if ( readyKey.isAcceptable())
        {
          accept( readyKey);
        }
        else
        {
          if ( readyKey.isReadable())
          {
            read( readyKey);
          }
          else if ( readyKey.isWritable())
          {
            write( readyKey);
          }
        }
      }
      catch( IOException e)
      {
        log.exception( e);
      }
    }
    
    return true;
  }
  
  /**
   * Accept an incoming connection.
   * @param key The selection key.
   */
  private void accept( SelectionKey key) throws IOException
  {
    createConnection( serverChannel.accept(), SelectionKey.OP_READ, listener);
  }
  
  /**
   * Close the server socket.
   */
  public void close()
  {
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
  private ITcpListener listener;
}
