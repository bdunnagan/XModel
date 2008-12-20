package org.xmodel.external.sql;

import java.sql.Connection;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * A class which implements connection pooling.
 */
public class ConnectionPool
{
  public ConnectionPool( Connection[] connections)
  {
    queue = new ArrayBlockingQueue<PooledConnection>( connections.length);
    for( Connection connection: connections)
    {
      PooledConnection pooled = new PooledConnection( this, connection);
      queue.offer( pooled);
    }
  }
  
  /**
   * Get a connection.
   * @return Returns a connection.
   */
  public Connection getConnection()
  {
    try
    {
      return queue.take();
    }
    catch( InterruptedException e)
    {
      return null;
    }
  }
  
  /**
   * Return a connection.
   * @param connection The connection.
   */
  public void release( PooledConnection connection)
  {
    if ( !queue.offer( connection)) throw new IllegalStateException( "Connection queue is full.");
  }
  
  private ArrayBlockingQueue<PooledConnection> queue;
}
