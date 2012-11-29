package org.xmodel.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import org.xmodel.GlobalSettings;
import org.xmodel.IDispatcher;

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
  }
  
  /**
   * Convenience method for creating with a fixed thread count.
   * @param threadCount The number of threads in the thread pool, use 0 for cached thread pool.
   */
  public ParallelExecutorDispatcher( int threadCount)
  {
    this.executor = createExecutor( threadCount);
  }
    
  /**
   * Create with the specified parameters.
   * @param executor The ExecutorService that will process dispatched Runnables.
   */
  public ParallelExecutorDispatcher( ExecutorService executor)
  {
    this.executor = executor;
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
    executor.execute( runnable);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IDispatcher#shutdown(boolean)
   */
  @Override
  public void shutdown( boolean immediate)
  {
    if ( immediate) executor.shutdownNow(); else executor.shutdown();
  }

  private ExecutorService executor;
}
