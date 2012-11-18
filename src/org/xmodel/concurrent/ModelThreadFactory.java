package org.xmodel.concurrent;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementation of ThreadFactory that adds a nice prefix and count to the thread names.
 */
public class ModelThreadFactory implements ThreadFactory
{
  public ModelThreadFactory( String prefix)
  {
    this.prefix = prefix;
  }

  /* (non-Javadoc)
   * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
   */
  @Override
  public Thread newThread( Runnable runnable)
  {
    return new Thread( runnable, prefix + "-" + counter.incrementAndGet());
  }

  private static AtomicInteger counter = new AtomicInteger( 0);
  
  private String prefix;
}
