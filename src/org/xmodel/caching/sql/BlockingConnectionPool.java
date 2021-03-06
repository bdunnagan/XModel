package org.xmodel.caching.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.xmodel.caching.sql.mbean.ConnectionPools;
import org.xmodel.log.Log;
import org.xmodel.log.SLog;

/**
 * A JDBC Connection pool.
 */
public class BlockingConnectionPool
{
  public BlockingConnectionPool( ISQLProvider provider, int minSize, int maxSize, int minWait, int maxWait)
  {
    // Make sure MySQL connect_timeout is larger than validateAfter!!
    this.validateAfter = 30000;
    this.maxSize = maxSize;
    this.minWait = minWait;
    this.maxWait = maxWait;
    this.count = new AtomicInteger( minSize);
    this.provider = provider;
    this.leased = new ConcurrentHashMap<Connection, Item>();
    this.queue = new LinkedBlockingQueue<Item>();
    for( int i=0; i<minSize; i++) queue.offer( new Item( i));
  }

  /**
   * Lease a Connection instance.
   * @return Returns the Connection instance.
   */
  public Connection lease()
  {
    try
    {
      Item item = queue.poll( minWait, TimeUnit.MILLISECONDS);
      if ( item == null)
      {
        if ( count.get() < maxSize)
        {
          int id = count.incrementAndGet();
          log.infof( "Expand JDBC connection pool: count=%d", id);
          item = new Item( id);
        }
        else
        {
          item = queue.poll( maxWait, TimeUnit.MILLISECONDS);
          while( item == null)
          {
            log.severe( "No available db connections in pool after waiting 30 seconds. Performing disaster recovery...");
            disasterRecovery();
            item = queue.poll( maxWait, TimeUnit.MILLISECONDS);
          }
        }
      }
      
      if ( item.connection == null) 
      {
        ConnectionPools.getInstance().incrementConnectionCount();
        item.connection = provider.newConnection();
        log.debugf( "Created JDBC connection: id=%d", item.id);
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
//      new Throwable().printStackTrace();
      leased.put( item.connection, item);
      
      ConnectionPools.getInstance().incrementLeasedCount();
      
      item.leasedOn = System.currentTimeMillis();
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
    ConnectionPools.getInstance().decrementLeasedCount();
    
    Item item = leased.get( connection);
    if ( item == null) throw new IllegalArgumentException();
    
    log.verbosef( "Returning JDBC connection, %d", item.id);
    item.validated = System.currentTimeMillis();
    
    item.leasedOn = 0;
    if ( !queue.offer( item))
      log.errorf( "Failed to enqueue released connection: id=%d", item.id);
  }
  
  /**
   * This method should never be called.  It is provided as a stop-gap to diagnose problems and hopefully
   * return the connection pool to a working state.
   */
  private void disasterRecovery()
  {
    log.info( "Turning on ConnectionPool debugging...");
    log.setLevel( Log.all);
    
    int nTotal = count.get();
    int nAvailable = queue.size();
    int nLeased = leased.size();
    
    log.infof( "total=%d, leased=%d, avail=%d", nTotal, nLeased, nAvailable);
    
    long ageLimit = System.currentTimeMillis() - 1 * 60 * 1000;
    for( Map.Entry<Connection, Item> entry: leased.entrySet())
    {
      Item item = entry.getValue();
      if ( item.leasedOn > 0 && item.leasedOn < ageLimit)
      {
        log.warnf( "Disaster recovery is closing connection [%X] that has been leased longer than 10 minutes!", item.connection.hashCode());
        
        try
        {
          item.connection.close();
        }
        catch( SQLException e)
        {
          log.errorf( "Disaster recovery failed to close connection: %s", e.toString());
        }
        
        item.leasedOn = 0;
        if ( !queue.offer( item))
          log.errorf( "Failed to enqueue released connection: id=%d", item.id);
      }
    }
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
    public long leasedOn;
    public Connection connection;
  }

  public final static Log log = Log.getLog( BlockingConnectionPool.class);
  
  private ISQLProvider provider;
  private int maxSize;
  private int minWait;
  private int maxWait;
  private AtomicInteger count;
  private BlockingQueue<Item> queue;
  private Map<Connection, Item> leased;
  private int validateAfter;
}
