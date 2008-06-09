/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.net;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import dunnagan.bob.xmodel.IDispatcher;

/**
 * An implementation of IDispatcher which allows runnables to be executed in the current thread.
 */
public class BlockingDispatcher implements IDispatcher
{
  public BlockingDispatcher()
  {
    queue = new ArrayBlockingQueue<Runnable>( 100);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.net.IDispatcher#execute(java.lang.Runnable)
   */
  public void execute( Runnable runnable)
  {
    try { queue.put( runnable);} catch( InterruptedException e) {}
  }
  
  /**
   * Dequeue and execute all the runnables on the queue.
   */
  public void process() throws InterruptedException
  {
    Runnable runnable = queue.take();
    while( runnable != null)
    {
      runnable.run();
      runnable = queue.poll();
    }
  }
  
  private BlockingQueue<Runnable> queue;
}
