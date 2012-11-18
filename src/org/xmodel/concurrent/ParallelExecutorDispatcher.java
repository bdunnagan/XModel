package org.xmodel.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import org.xmodel.GlobalSettings;
import org.xmodel.IDispatcher;
import org.xmodel.IModel;

/**
 * An IDispatcher implementation that wraps an ExecutorService.  Locking must be used within
 * the Runnables that are dispatched with this class.  Note that the order in which Runnables
 * are executed is not guaranteed.
 */
public class ParallelExecutorDispatcher implements IDispatcher
{
  /**
   * Convenience method for creating with a fixed thread count.
   * @param model The model.
   * @param threadCount The number of threads in the thread pool, use 0 for cached thread pool.
   */
  public ParallelExecutorDispatcher( IModel model, int threadCount)
  {
    this( model, createExecutor( threadCount));
  }
    
  /**
   * Create with the specified parameters.
   * @param model The model.
   * @param executor The ExecutorService that will process dispatched Runnables.
   */
  public ParallelExecutorDispatcher( IModel model, ExecutorService executor)
  {
    this.model = model;
    this.registry = GlobalSettings.getInstance();
    this.executor = executor;
  }
  
  /**
   * Create the ExecutorServie.
   * @param threadCount The number of threads in the thread pool, use 0 for cached thread pool.
   * @return Returns the new ExecutorService.
   */
  private static ExecutorService createExecutor( int threadCount)
  {
    ThreadFactory factory = new ModelThreadFactory( "model-parallel");
    return (threadCount == 0)? Executors.newCachedThreadPool( factory): Executors.newFixedThreadPool( threadCount, factory);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IDispatcher#execute(java.lang.Runnable)
   */
  @Override
  public void execute( Runnable runnable)
  {
    try
    {
      model.setThread( Thread.currentThread());
      registry.setModel( model);
      
      executor.execute( runnable);
    }
    finally
    {
      registry.setModel( null);
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

  private IModel model;
  private GlobalSettings registry;
  private ExecutorService executor;
}
