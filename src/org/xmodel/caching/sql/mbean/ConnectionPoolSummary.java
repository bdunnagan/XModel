package org.xmodel.caching.sql.mbean;

import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.xmodel.log.SLog;

public class ConnectionPoolSummary implements ConnectionPoolSummaryMBean
{
  protected ConnectionPoolSummary()
  {
    totalCount = new AtomicInteger( 0);
    leaseCount = new AtomicInteger( 0);
    
    try
    {
      MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
      ObjectName name = new ObjectName( "org.xmodel.caching.sql.mbean:type=ConnectionPoolSummary");   
      mbs.registerMBean( this, name);
    }
    catch( Exception e)
    {
      SLog.exception( this, e);
    }
  }
  
  /**
   * @return Returns the singleton instance.
   */
  public static ConnectionPoolSummary getInstance()
  {
    return instance;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.caching.sql.mbean.IConnectionPoolMBean#getConnectionCount()
   */
  public int getConnectionCount()
  {
    return totalCount.get();
  }

  /**
   * Increment the connection count.
   */
  public void incrementConnectionCount()
  {
    totalCount.incrementAndGet();
  }
  
  /**
   * Decrement the connection count.
   */
  public void decrementConnectionCount()
  {
    totalCount.decrementAndGet();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.caching.sql.mbean.IConnectionPoolMBean#getLeasedCount()
   */
  public int getLeasedCount()
  {
    return leaseCount.get();
  }
  
  /**
   * Increment the leased connection count.
   */
  public void incrementLeasedCount()
  {
    leaseCount.incrementAndGet();
  }
  
  /**
   * Decrement the leased connection count.
   */
  public void decrementLeasedCount()
  {
    leaseCount.decrementAndGet();
  }
  
  private static ConnectionPoolSummary instance = new ConnectionPoolSummary();
  
  private AtomicInteger totalCount;
  private AtomicInteger leaseCount;
}
