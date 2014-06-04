package org.xmodel.util;

public interface CountingThreadPoolExecutorMBean
{
  /**
   * @return Returns the throughput over the last ten submitted tasks.
   */
  public float getTenRateIn();
  
  /**
   * @return Returns the throughput over the last thousand submitted tasks.
   */
  public float getThousandRateIn();
  
  /**
   * @return Returns the throughput over the last ten submitted tasks.
   */
  public float getTenRateOut();
  
  /**
   * @return Returns the throughput over the last thousand submitted tasks.
   */
  public float getThousandRateOut();
  
  /**
   * @return Returns the approximate number of submitted tasks.
   */
  public long getTaskCount();
  
  /**
   * @return Returns the number of threads currently executing tasks.
   */
  public int getActiveCount();

  /**
   * @return Returns the approximate number of completed tasks.
   */
  public long getCompletedTaskCount();
  
  /**
   * @return Returns the initial/minimum number of threads in the pool.
   */
  public int getCorePoolSize();
  
  /**
   * @return Returns the maximum number of threads in the pool.
   */
  public int getMaximumPoolSize();
  
  /**
   * @return Returns the largest number of threads in the pool.
   */
  public int getLargestPoolSize();
  
  /**
   * @return Returns the current number of threads in the pool.
   */
  public int getPoolSize();
}
