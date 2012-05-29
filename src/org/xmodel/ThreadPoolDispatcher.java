package org.xmodel;

import java.util.concurrent.ExecutorService;

/**
 * An implementation of IDispatcher that uses an instance of ExecutorService to execute Runnables.
 */
public class ThreadPoolDispatcher implements IDispatcher
{
  public ThreadPoolDispatcher( ExecutorService executor)
  {
    this.executor = executor;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IDispatcher#execute(java.lang.Runnable)
   */
  @Override
  public void execute( Runnable runnable)
  {
    executor.execute( runnable);
  }
  
  /**
   * Shutdown thread pool.
   */
  public void shutdown()
  {
    executor.shutdown();
  }
  
  private ExecutorService executor;
}