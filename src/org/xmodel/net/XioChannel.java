package org.xmodel.net;

import java.io.IOException;
import java.net.SocketAddress;

import org.jboss.netty.handler.ssl.SslHandler;
import org.xmodel.future.AsyncFuture;
import org.xmodel.net.connection.INetworkConnection;
import org.xmodel.net.connection.INetworkMessage;

public class XioChannel implements INetworkConnection
{
  public XioChannel( INetworkConnection connection)
  {
    this.connection = connection;
  }
  
  /**
   * Set the peer.
   * @param peer The peer.
   */
  public void setPeer( XioPeer peer)
  {
    this.peer = peer;
  }
  
  /**
   * @return Returns the peer of this connection.
   */
  public XioPeer getPeer()
  {
    return peer;
  }
  
  /**
   * @return Returns null or the SslHandler instance.
   */
  public SslHandler getSslHandler()
  {
    return null;
  }
  
  /**
   * @return Returns the local address of the connection.
   */
  public SocketAddress getLocalAddress()
  {
    return null;
  }
  
  /**
   * @return Returns the remote address of the connection.
   */
  public SocketAddress getRemoteAddress()
  {
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.connection.INetworkConnection#connect()
   */
  @Override
  public AsyncFuture<INetworkConnection> connect()
  {
    return connection.connect();
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.connection.INetworkConnection#close()
   */
  @Override
  public AsyncFuture<INetworkConnection> close()
  {
    return connection.close();
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.connection.INetworkConnection#getCloseFuture()
   */
  @Override
  public AsyncFuture<INetworkConnection> getCloseFuture()
  {
    return connection.getCloseFuture();
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.connection.INetworkConnection#send(org.xmodel.net.connection.INetworkMessage)
   */
  @Override
  public void send( INetworkMessage message) throws IOException
  {
    connection.send( message);
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.connection.INetworkConnection#request(org.xmodel.net.connection.INetworkMessage)
   */
  @Override
  public AsyncFuture<INetworkMessage> request( INetworkMessage request)
  {
    return connection.request( request);
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.connection.INetworkConnection#addListener(org.xmodel.net.connection.INetworkConnection.IListener)
   */
  @Override
  public void addListener( IListener listener)
  {
    connection.addListener( listener);
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.connection.INetworkConnection#removeListener(org.xmodel.net.connection.INetworkConnection.IListener)
   */
  @Override
  public void removeListener( IListener listener)
  {
    connection.removeListener( listener);
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.connection.INetworkConnection#getListeners()
   */
  @Override
  public IListener[] getListeners()
  {
    return connection.getListeners();
  }

  private XioPeer peer;
  private INetworkConnection connection;
}
