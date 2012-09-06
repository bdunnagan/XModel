package org.xmodel.concurrent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.xmodel.IDispatcher;
import org.xmodel.IModel;
import org.xmodel.Model;
import org.xmodel.ModelRegistry;
import org.xmodel.log.SLog;

/**
 * An implementation of IDispatcher must guarantee that Runnables are executed sequentially in the order
 * that they were received, and that the same instance of IModel is always available from the ModelRegistry
 * when Runnables execute.  IModel instances are stored in thread-local data in ModelRegistry.  Classes
 * in the XModel framework are allowed to cache this instance.  ThreadPoolDispatcher meets these 
 * requirements by installing the same instance of IModel into ModelRegistry before executing each
 * dispatched Runnable.  In this way, ThreadPoolDispatcher provides full support for listener notification
 * as long as all mutative operations are dispatched using the same ThreadPoolDispatcher instance.
 * <p>
 * In order to guarantee the sequential execution of dispatched Runnables, ThreadPoolDispatcher queues
 * Runnables into its own BlockingQueue.  This queue is repeatedly submitted to the ExecutorService when
 * it is non-empty.  The target thread removes one Runnable from the ThreadPoolDispatcher's BlockingQueue.
 * Thus, sequential execution is guaranteed while taking advantage of the inherent load-balancing of a
 * thread-pool.  This makes ThreadPoolDispatcher an appropriate choice when many discreet models need to
 * be processed by a smaller number of threads.
 */
public class ThreadPoolDispatcher implements IDispatcher, Runnable
{
  /**
   * Convenience method for creating a ThreadPoolDispatcher with a fixed thread count and unbounded queue.
   * @param threadCount The number of threads in the thread pool.
   */
  public ThreadPoolDispatcher( int threadCount)
  {
    this( new Model(), Executors.newFixedThreadPool( threadCount), new LinkedBlockingQueue<Runnable>());
  }
  
  /**
   * Convenience method for creating a ThreadPoolDispatcher with a fixed thread count and limited queue.
   * LinkedBlockingQueue is used, so there is no fairness policy.
   * @param threadCount The number of threads in the thread pool.
   * @param queueSize The dispatch queue size.
   */
  public ThreadPoolDispatcher( int threadCount, int queueSize)
  {
    this( threadCount, queueSize, false);
  }
  
  /**
   * Convenience method for creating a ThreadPoolDispatcher with a fixed thread count, limited queue, and
   * optional fairness policy.  LinkedBlockingQueue is used unless a fairness policy is requested, in which
   * case ArrayBlockingQueue is used.
   * @param threadCount The number of threads in the thread pool.
   * @param queueSize The dispatch queue size.
   * @param fair True if a fairness policy should be used.
   */
  public ThreadPoolDispatcher( int threadCount, int queueSize, boolean fair)
  {
    this( new Model(), Executors.newFixedThreadPool( threadCount), 
      fair? new ArrayBlockingQueue<Runnable>( queueSize, true): 
            new LinkedBlockingQueue<Runnable>( queueSize));
  }
  
  /**
   * Create a ThreadPoolDispatcher with the specified parameters.
   * @param model The model.
   * @param executor The ExecutorService that will process dispatched Runnables.
   * @param queue The queue implementation.
   */
  public ThreadPoolDispatcher( IModel model, ExecutorService executor, BlockingQueue<Runnable> queue)
  {
    this.model = model;
    this.model.setDispatcher( this);
    
    this.executor = executor;
    this.registry = ModelRegistry.getInstance();
    this.queue = queue;
    this.queueSize = new AtomicInteger( 0);
    this.lock = new ReentrantLock();
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
      SLog.exception( this, e);
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
  
  /**
   * Acquire the lock for the model. Once this lock is acquired, runnables will not be processed
   * by the ExecutorService until the lock is released.
   */
  public void lock() throws InterruptedException
  {
    if ( !lock.tryLock())
    {
      SLog.verbosef( this, "Lock.tryLock failed, waiting to acquire lock...");
      
      if ( !lock.tryLock( 5, TimeUnit.MINUTES))
      {
        SLog.severe( this, "tryLock timed-out!");
        throw new IllegalStateException();
      }
      
      SLog.verbosef( this, "Lock acquired.");
    }
  }
  
  /**
   * Release lock for the model.
   */
  public void unlock()
  {
    lock.unlock();
  }
  
  /* (non-Javadoc)
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run()
  {
    try
    {
      lock();    
    }
    catch( InterruptedException e)
    {
      SLog.warn( this, "Lock acquisition interrupted.");
    }
    
    try
    {
      model.setThread( Thread.currentThread());
      registry.setModel( model);
      
      Runnable runnable = queue.poll();
      if ( runnable == null) throw new IllegalStateException();
      
      runnable.run();
    }
    finally
    {
      lock.unlock();
      
      if ( queueSize.decrementAndGet() > 0) 
        executor.execute( this);
    }
  }
  
  private ExecutorService executor;
  protected IModel model;
  private ModelRegistry registry;
  private BlockingQueue<Runnable> queue;
  private AtomicInteger queueSize;
  private Lock lock;
}
