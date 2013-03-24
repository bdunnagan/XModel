package org.xmodel;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import org.xmodel.concurrent.Statistics;
import org.xmodel.log.Log;

/**
 * An implementation of Executor that dispatches runnables to a queue that can be manually pumped.
 */
public class CurrentThreadExecutor implements Executor
{
  public CurrentThreadExecutor()
  {
    queue = new LinkedBlockingQueue<Runnable>();
    statistics = new Statistics( log);
  }
  
  /* (non-Javadoc)
   * @see java.util.concurrent.Executor#execute(java.lang.Runnable)
   */
  @Override
  public void execute( Runnable runnable)
  {
    long now = System.nanoTime();
    long latency = now - lastProcessTime;
    if ( latency > maxLatencyWarning)
      log.warnf( "CurrentThreadExecutor.process() not called for %dms", latency / (long)1e6);
    
    queue.offer( runnable);
  }
  
  /**
   * Process one Runnable in the queue blocking if necessary.
   */
  public void process() throws InterruptedException
  {
    lastProcessTime = System.nanoTime();
    
    Runnable runnable = queue.take();
    if ( log.debug())
    {
      try
      {
        statistics.executionStarted();
        runnable.run();
      }
      finally
      {
        statistics.executionFinished();
      }
    }
    else
    {
      runnable.run();
    }
  }
  
  private final static Log log = Log.getLog( CurrentThreadExecutor.class);
  private final static long maxLatencyWarning = (long)5e9;
  
  private BlockingQueue<Runnable> queue;
  private long lastProcessTime;
  private Statistics statistics;
}
