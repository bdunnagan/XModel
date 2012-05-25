package org.xmodel.caching.sql;

import java.sql.Connection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.xmodel.log.SLog;

/**
 * A JDBC Connection pool.
 */
public class ConnectionPool
{
  public ConnectionPool( ISQLProvider provider, int size)
  {
    this.provider = provider;
    this.queue = new ArrayBlockingQueue<Item>( size);
    for( int i=0; i<size; i++) queue.offer( new Item( null));
  }

  /**
   * Lease a Connection instance.
   * @return Returns the Connection instance.
   */
  public Connection lease()
  {
    try
    {
      Item item = queue.take();
      if ( item.connection == null)
        item.connection = provider.newConnection();
      return item.connection;
    }
    catch( InterruptedException e)
    {
      SLog.exception( this, e);
      return null;
    }
  }
  
  /**
   * Return a Connection instance to the pool.
   * @param connection The Connection instance.
   */
  public void release( Connection connection)
  {
    queue.offer( new Item( connection));
  }
  
  private static class Item
  {
    public Item( Connection connection)
    {
      this.connection = connection;
    }
    
    public Connection connection;
  }

  private ISQLProvider provider;
  private BlockingQueue<Item> queue;
}
