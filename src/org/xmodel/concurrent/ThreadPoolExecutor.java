package org.xmodel.concurrent;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import org.xmodel.log.Log;
import org.xmodel.util.CountingThreadPoolExecutor;

/**
 * An implementation of Executor that delegates to another Executor and collects and logs statistics.
 */
public class ThreadPoolExecutor implements Executor
{
  /**
   * Convenience method for creating with a fixed thread count.
   * @param name The prefix for the names of threads in the thread pool.
   * @param threadCount The number of threads in the thread pool, use 0 for cached thread pool.
   */
  public ThreadPoolExecutor( String name, int threadCount)
  {
    this.executor = createExecutor( name, threadCount);
    this.statistics = new Statistics( log);
  }
    
  /**
   * Create the ExecutorService.
   * @param name The prefix for the names of threads in the thread pool.
   * @param threadCount The number of threads in the thread pool, use 0 for cached thread pool.
   * @return Returns the new ExecutorService.
   */
  private ExecutorService createExecutor( String name, int threadCount)
  {
    return (threadCount == 0)?
        new CountingThreadPoolExecutor( name, 0, Integer.MAX_VALUE, 60):
        new CountingThreadPoolExecutor( name, threadCount, threadCount, 60);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IDispatcher#execute(java.lang.Runnable)
   */
  @Override
  public void execute( Runnable runnable)
  {
    if ( log.debug())
    {
      try
      {
        statistics.executionStarted();
        executor.execute( runnable);
      }
      finally
      {
        statistics.executionFinished();
      }
    }
    else
    {
      executor.execute( runnable);
    }
  }
  
  private static Log log = Log.getLog( ThreadPoolExecutor.class);

  private Executor executor;
  private Statistics statistics;
}
