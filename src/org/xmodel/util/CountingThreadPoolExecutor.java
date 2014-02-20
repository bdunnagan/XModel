package org.xmodel.util;

import java.lang.management.ManagementFactory;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.xmodel.log.SLog;

/**
 * A ThreadPoolExecutor that keeps track of the number of tasks submitted, and the number of tasks that have finished executing.
 */
public class CountingThreadPoolExecutor extends ThreadPoolExecutor implements CountingThreadPoolExecutorMBean
{
  /**
   * Create the appropriate queue depending on whether maximum size of the pool is bounded.
   * @param maxSize The maximum size of the pool.
   * @return Returns a new blocking queue.
   */
  private static BlockingQueue<Runnable> createQueue( int maxSize)
  {
    return (maxSize > 0 && maxSize < Integer.MAX_VALUE)?
      new LinkedBlockingQueue<Runnable>():
      new SynchronousQueue<Runnable>();
  }
  
  /**
   * Create thread pool with the specified parameters and a SynchronousQueue.
   * @param poolName The name of the thread pool (used to prefix thread names).
   * @param coreSize The initial number of threads in the pool.
   * @param maxSize The maximum number of threads in the pool.
   * @param linger The idle time (in seconds) after which threads are destroyed.
   */
  public CountingThreadPoolExecutor( String poolName, int coreSize, int maxSize, int linger)
  {
    super( coreSize, maxSize, linger, TimeUnit.SECONDS, createQueue( maxSize), new PrefixThreadFactory( poolName));
    
    try
    {
      MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
      String sanitizedName = poolName.replace( '-', '_');
      ObjectName name = new ObjectName( "org.xmodel.util:type=CountingThreadPoolExecutor,name="+sanitizedName);   
      mbs.registerMBean( this, name);
    }
    catch( Exception e)
    {
      SLog.warnf( this, "Failed to register mbean: %s", e.toString());
    }
  }
}
