package org.xmodel.concurrent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import org.xmodel.GlobalSettings;
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
   * @param model The model.
   * @param threadCount The number of threads in the thread pool, use 0 for cached thread pool.
   */
  public SerialExecutorDispatcher( IModel model, int threadCount)
  {
    this( model, createExecutor( threadCount), new LinkedBlockingQueue<Runnable>());
  }
  
  /**
   * Convenience method for creating with a fixed thread count, limited queue, and
   * optional fairness policy.  LinkedBlockingQueue is used unless a fairness policy is requested, in which
   * case ArrayBlockingQueue is used.
   * @param model The model.
   * @param threadCount The number of threads in the thread pool.
   * @param queueSize The dispatch queue size.
   * @param fair True if a fairness policy should be used.
   */
  public SerialExecutorDispatcher( IModel model, int threadCount, int queueSize, boolean fair)
  {
    this( new Model(), createExecutor( threadCount), 
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
    this.queueSize = new AtomicInteger( 0);
    
    this.statistics = new Statistics();
  }
  
  /**
   * Create the ExecutorServie.
   * @param threadCount The number of threads in the thread pool, use 0 for cached thread pool.
   * @return Returns the new ExecutorService.
   */
  private static ExecutorService createExecutor( int threadCount)
  {
    ThreadFactory factory = new ModelThreadFactory( "model-serial");
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
      queue.put( runnable);
    }
    catch( InterruptedException e)
    {
      SLog.warnf( this, "Thread interrupted and runnable was not dispatched.");
      return;
    }
    
    if ( queueSize.getAndIncrement() == 0)
      executor.execute( this);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IDispatcher#shutdown(boolean)
   */
  @Override
  public void shutdown( boolean immediate)
  {
    if ( immediate) executor.shutdownNow(); else executor.shutdown();
  }
  
  @Override
  public void run()
  {
    if ( log.debug()) statistics.executionStarted();
    
    try
    {
      model.setThread( Thread.currentThread());
      if ( registry == null) registry = GlobalSettings.getInstance();
      registry.setModel( model);
      
      Runnable runnable = queue.poll();
      if ( runnable == null) throw new IllegalStateException();
      
      runnable.run();
    }
    finally
    {
      registry.setModel( null);
      
      if ( queueSize.decrementAndGet() > 0) 
        executor.execute( this);
      
      if ( log.debug()) statistics.executionFinished();
    }
  }
  
  private static Log log = Log.getLog( SerialExecutorDispatcher.class);
  
  private ExecutorService executor;
  protected IModel model;
  private BlockingQueue<Runnable> queue;
  private AtomicInteger queueSize;
  private GlobalSettings registry;
  private Statistics statistics;
}
