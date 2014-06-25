package org.xmodel.caching.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import org.xmodel.caching.sql.mbean.ConnectionPools;
import org.xmodel.log.Log;
import org.xmodel.log.SLog;

/**
 * A JDBC Connection pool.
 */
public class ThreadConnectionPool
{
  public ThreadConnectionPool( ISQLProvider provider)
  {
    this.validateAfter = 60000;
    this.provider = provider;
    this.pool = new ThreadLocal<Item>();
    this.counter = new AtomicInteger();
  }

  /**
   * Lease a Connection instance.
   * @return Returns the Connection instance.
   */
  public Connection lease()
  {
    Item item = pool.get();
    if ( item != null)
    {
      long now = System.currentTimeMillis();
      if ( (now - item.validated) > validateAfter)
      {
        log.debugf( "Validating JDBC connection: id=%d, n=%d", item.id, counter.get());
        if ( !validate( item.connection))
        {
          item.connection = provider.newConnection();
          log.debugf( "Renewed invalid JDBC connection: id=%d, n=%d", item.id, counter.get());
        }
        else
        {
          item.validated = System.currentTimeMillis();
        }
      }
    }
    else
    {
      item = new Item( counter.incrementAndGet());
      item.connection = provider.newConnection();
      item.validated = System.currentTimeMillis();
      pool.set( item);
    }

    ConnectionPools.getInstance().incrementLeasedCount();
    log.verbosef( "Leasing JDBC connection: id=%d, n=%d", item.id, counter.get());
    return item.connection;
  }
  
  /**
   * Return a Connection instance to the pool.
   * @param connection The Connection instance.
   */
  public void release( Connection connection)
  {
  }

  /**
   * Validate the connection is still usable.
   * @param connection The connection.
   * @return Returns true if the connection is still valid.
   */
  private boolean validate( Connection connection)
  {
    try
    {
      if (connection.isValid( 0)) return true;
    }
    catch( SQLException e)
    {
      SLog.warnf( this, e.toString());
    }
    
    try
    {
      connection.close();
    }
    catch( SQLException e)
    {
      SLog.warnf( this, e.toString());
    }
    
    return false;
  }
  
  private static class Item
  {
    public Item( int id)
    {
      this.id = id;
      this.validated = System.currentTimeMillis();
    }
    
    public int id;
    public long validated;
    public Connection connection;
  }

  public final static Log log = Log.getLog( ThreadConnectionPool.class);
  
  private ISQLProvider provider;
  private ThreadLocal<Item> pool;
  private AtomicInteger counter;
  private int validateAfter;
}
