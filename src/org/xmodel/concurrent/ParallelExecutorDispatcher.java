package org.xmodel.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import org.xmodel.GlobalSettings;
import org.xmodel.IDispatcher;
import org.xmodel.log.Log;

/**
 * An IDispatcher implementation that wraps an ExecutorService.  Locking must be used within
 * the Runnables that are dispatched with this class.  Note that the order in which Runnables
 * are executed is not guaranteed.
 */
public class ParallelExecutorDispatcher implements IDispatcher
{
  /**
   * Default constructor, which must be followed with a call to <code>configure</code>.
   */
  public ParallelExecutorDispatcher()
  {
    this.statistics = new Statistics();
  }
  
  /**
   * Convenience method for creating with a fixed thread count.
   * @param threadCount The number of threads in the thread pool, use 0 for cached thread pool.
   */
  public ParallelExecutorDispatcher( int threadCount)
  {
    this.executor = createExecutor( threadCount);
    this.statistics = new Statistics();
  }
    
  /**
   * Create with the specified parameters.
   * @param executor The ExecutorService that will process dispatched Runnables.
   */
  public ParallelExecutorDispatcher( ExecutorService executor)
  {
    this.executor = executor;
    this.statistics = new Statistics();
  }
  
  /**
   * Create the ExecutorService.
   * @param threadCount The number of threads in the thread pool, use 0 for cached thread pool.
   * @return Returns the new ExecutorService.
   */
  private ExecutorService createExecutor( int threadCount)
  {
    ThreadFactory factory = new ModelThreadFactory( "model-parallel", new Runnable() {
      public void run()
      {
        GlobalSettings.getInstance().getModel().setDispatcher( ParallelExecutorDispatcher.this);
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

  /* (non-Javadoc)
   * @see org.xmodel.IDispatcher#shutdown(boolean)
   */
  @Override
  public void shutdown( boolean immediate)
  {
    if ( immediate) executor.shutdownNow(); else executor.shutdown();
  }
  
  private static Log log = Log.getLog( ParallelExecutorDispatcher.class);

  private ExecutorService executor;
  private Statistics statistics;
}
