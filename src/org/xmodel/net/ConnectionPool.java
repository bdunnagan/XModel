package org.xmodel.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A generic connection pool that provides the following features:
 * <ul>
 * <li>Dynamic growth of connection pool size.</li>
 * <li>Timed expiry of connections for dynamic contraction.</li>
 * <li>Monitoring of the health of each connection.</li>
 * </ul>
 */
public abstract class ConnectionPool<T>
{
  public ConnectionPool()
  {
    queue = new LinkedBlockingQueue<T>();
  }

  /**
   * Lease a connection from the pool growing the connection pool if necessary.
   * @return Returns a connection.
   */
  public T lease() throws IOException
  {
    T connection = queue.poll();
    if ( connection == null)
    {
      connection = create( address);
      if ( !queue.offer( connection)) throw new IllegalStateException();
      connections.add( connection);
    }
    return connection;
  }
  
  /**
   * Return the specified connection to the pool.
   * @param connection The connection.
   * @param healthy True if the connection is considered to be healthy.
   */
  public void release( T connection, boolean healthy)
  {
    if ( healthy)
    {
      if ( !queue.offer( connection))
      {
        discard( connection);
        throw new IllegalStateException();
      }
    }
    else
    {
      discard( connection);
    }
  }

  /**
   * Create a new connection.
   * @param address The remote address.
   * @return Returns the new connection.
   */
  protected abstract T create( InetSocketAddress address) throws IOException;
  
  /**
   * Close and discard the specified connection.
   * @param connection The connection.
   */
  protected abstract void discard( T connection);
  
  private InetSocketAddress address;
  private BlockingQueue<T> queue;
  private List<T> connections;
}
