package org.xmodel.concurrent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.xmodel.IDispatcher;
import org.xmodel.IModel;
import org.xmodel.Model;
import org.xmodel.log.Log;
import org.xmodel.log.SLog;

/**
 * An IDispatcher implementation that guarantees the serial execution of dispatched Runnables. ThreadPoolDispatcher 
 * queues Runnables into its own BlockingQueue.  This queue is repeatedly submitted to the ExecutorService when
 * it is non-empty.  The target thread removes one Runnable from the ThreadPoolDispatcher's BlockingQueue and
 * executes it.  ThreadPoolDispatcher is an appropriate choice when many independent models need to be processed 
 * by a smaller number of threads.
 */
public class SerialExecutorDispatcher implements IDispatcher, Runnable
{
  /**
   * Convenience method for creating with a fixed thread count and unbounded queue.
   * @param name The prefix for the names of threads in the thread pool.
   * @param model The model.
   * @param threadCount The number of threads in the thread pool, use 0 for cached thread pool.
   */
  public SerialExecutorDispatcher( String name, IModel model, int threadCount)
  {
    this( model, createExecutor( name, threadCount), new LinkedBlockingQueue<Runnable>());
  }
  
  /**
   * Convenience method for creating with a fixed thread count, limited queue, and
   * optional fairness policy.  LinkedBlockingQueue is used unless a fairness policy is requested, in which
   * case ArrayBlockingQueue is used.
   * @param name The prefix for the names of threads in the thread pool.
   * @param model The model.
   * @param threadCount The number of threads in the thread pool.
   * @param queueSize The dispatch queue size.
   * @param fair True if a fairness policy should be used.
   */
  public SerialExecutorDispatcher( String name, IModel model, int threadCount, int queueSize, boolean fair)
  {
    this( new Model(), createExecutor( name, threadCount), 
      fair? new ArrayBlockingQueue<Runnable>( queueSize, true): 
            new LinkedBlockingQueue<Runnable>( queueSize));
  }
  
  /**
   * Create with the specified parameters.
   * @param model The model.
   * @param executor The ExecutorService that will process dispatched Runnables.
   * @param queue The queue implementation.
   */
  public SerialExecutorDispatcher( IModel model, ExecutorService executor, BlockingQueue<Runnable> queue)
  {
    this.model = model;
    this.model.setDispatcher( this);
    
    this.executor = executor;
    this.queue = queue;
    
    this.statistics = new Statistics();
  }
  
  /**
   * Create the ExecutorServie.
   * @param name The prefix for the names of threads in the thread pool.
   * @param threadCount The number of threads in the thread pool, use 0 for cached thread pool.
   * @return Returns the new ExecutorService.
   */
  private static ExecutorService createExecutor( String name, int threadCount)
  {
    ThreadFactory factory = new ModelThreadFactory( name);
    return (threadCount == 0)? Executors.newCachedThreadPool( factory): Executors.newFixedThreadPool( threadCount, factory);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IDispatcher#execute(java.lang.Runnable)
   */
  @Override
  public void execute( Runnable runnable)
  {
    boolean wasEmpty = queue.isEmpty();
    
    try
    {
      queue.put( runnable);
    }
    catch( InterruptedException e)
    {
      SLog.warnf( this, "Thread interrupted and runnable was not dispatched.");
      return;
    }
    
    if ( wasEmpty) executor.execute( this);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IDispatcher#shutdown(boolean)
   */
  @Override
  public void shutdown( boolean immediate)
  {
    if ( immediate) executor.shutdownNow(); else executor.shutdown();
  }
  
  /* (non-Javadoc)
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run()
  {
    if ( log.debug()) 
    {
      statistics.executionStarted();
      log.debugf( "Queued: %d", queue.size());
    }
    
    try
    {
      model.writeLock( 5, TimeUnit.MINUTES);
      
      Runnable runnable = queue.poll();
      if ( runnable != null) runnable.run();
    }
    catch( InterruptedException e)
    {
      log.exception( e);
    }
    finally
    {
      model.writeUnlock();
      
      if ( !queue.isEmpty()) executor.execute( this);
      
      if ( log.debug()) statistics.executionFinished();
    }
  }
  
  private static Log log = Log.getLog( SerialExecutorDispatcher.class);
  
  private ExecutorService executor;
  protected IModel model;
  private BlockingQueue<Runnable> queue;
  private Statistics statistics;
}
