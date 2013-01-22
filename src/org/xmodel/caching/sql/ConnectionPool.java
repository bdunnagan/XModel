package org.xmodel.caching.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import org.xmodel.log.Log;
import org.xmodel.log.SLog;

/**
 * A JDBC Connection pool.
 */
public class ConnectionPool
{
  public ConnectionPool( ISQLProvider provider, int size)
  {
    this.validateAfter = 60000;
    
    this.provider = provider;
    this.leased = new ConcurrentHashMap<Connection, Item>();
    this.queue = new LinkedBlockingQueue<Item>();
    for( int i=0; i<size; i++) queue.offer( new Item( i));
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
      {
        item.connection = provider.newConnection();
        log.debugf( "Created JDBC connection, %d", item.id);
      }
      
      long now = System.currentTimeMillis();
      if ( (now - item.validated) > validateAfter)
      {
        log.debugf( "Validating JDBC connection, %d", item.id);
        if ( !validate( item.connection))
        {
          item.connection = provider.newConnection();
          log.debugf( "Renewed invalid JDBC connection, %d", item.id);
        }
        else
        {
          item.validated = System.currentTimeMillis();
        }
      }
  
      log.verbosef( "Leasing JDBC connection, %d", item.id);
      leased.put( item.connection, item);
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
    Item item = leased.get( connection);
    if ( item == null) throw new IllegalArgumentException();
    log.verbosef( "Returning JDBC connection, %d", item.id);
    item.validated = System.currentTimeMillis();
    queue.offer( item);
  }
  
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

  private final static Log log = Log.getLog( ConnectionPool.class);
  
  private ISQLProvider provider;
  private BlockingQueue<Item> queue;
  private Map<Connection, Item> leased;
  private int validateAfter;
}
