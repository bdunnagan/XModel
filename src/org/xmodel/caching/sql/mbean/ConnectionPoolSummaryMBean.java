package org.xmodel.caching.sql.mbean;

public interface ConnectionPoolSummaryMBean
{
  /**
   * @return Returns the number JDBC connections in the pool.
   */
  public int getConnectionCount();

  /**
   * @return Returns the number of JDBC connections currently leased.
   */
  public int getLeasedCount();
}