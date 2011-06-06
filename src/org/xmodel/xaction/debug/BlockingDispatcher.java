package org.xmodel.xaction.debug;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.xmodel.IDispatcher;

public class BlockingDispatcher implements IDispatcher
{
  /**
   * Create a BlockingDispatcher with the specified queue depth.
   * @param count The depth of the queue.
   */
  public BlockingDispatcher( int count)
  {
    queue = new ArrayBlockingQueue<Runnable>( count);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IDispatcher#execute(java.lang.Runnable)
   */
  @Override
  public void execute( Runnable runnable)
  {
    try { queue.put( runnable);} catch( InterruptedException e) {}
  }

  /**
   * Process the next item on the queue waiting for the specified timeout.
   * @param timeout The timeout in milliseconds.
   */
  public void process( int timeout)
  {
    try
    {
      Runnable runnable = queue.poll( timeout, TimeUnit.MILLISECONDS);
      if ( runnable != null) runnable.run();
    }
    catch( InterruptedException e)
    {
    }
  }
  
  private BlockingQueue<Runnable> queue;
}
