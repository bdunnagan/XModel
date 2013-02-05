package org.xmodel.concurrent;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import org.xmodel.GlobalSettings;
import org.xmodel.log.Log;

/**
 * An implementation of Executor that delegates to another Executor and collects and logs statistics.
 */
public class LoggingExecutorWrapper implements Executor
{
  /**
   * Convenience method for creating with a fixed thread count.
   * @param name The prefix for the names of threads in the thread pool.
   * @param threadCount The number of threads in the thread pool, use 0 for cached thread pool.
   */
  public LoggingExecutorWrapper( String name, int threadCount)
  {
    this.executor = createExecutor( name, threadCount);
    this.statistics = new Statistics();
  }
    
  /**
   * Create with the specified parameters.
   * @param executor The ExecutorService that will process dispatched Runnables.
   */
  public LoggingExecutorWrapper( Executor executor)
  {
    this.executor = executor;
    this.statistics = new Statistics();
  }
  
  /**
   * Create the ExecutorService.
   * @param name The prefix for the names of threads in the thread pool.
   * @param threadCount The number of threads in the thread pool, use 0 for cached thread pool.
   * @return Returns the new ExecutorService.
   */
  private ExecutorService createExecutor( String name, int threadCount)
  {
    ThreadFactory factory = new SimpleThreadFactory( name, new Runnable() {
      public void run()
      {
        GlobalSettings.getInstance().getModel().setExecutor( LoggingExecutorWrapper.this);
      }
    });
    return (threadCount == 0)? Executors.newCachedThreadPool( factory): Executors.newFixedThreadPool( threadCount, factory);
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
  
  private static Log log = Log.getLog( LoggingExecutorWrapper.class);

  private Executor executor;
  private Statistics statistics;
}
