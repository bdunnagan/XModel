package org.xmodel.concurrent;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementation of ThreadFactory that adds a nice prefix and count to the thread names.
 */
public class SimpleThreadFactory implements ThreadFactory
{
  public SimpleThreadFactory( String prefix)
  {
    this( prefix, null);
  }

  public SimpleThreadFactory( String prefix, Runnable setup)
  {
    this.prefix = prefix;
    this.setup = setup;
  }

  /* (non-Javadoc)
   * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
   */
  @Override
  public Thread newThread( final Runnable runnable)
  {
    String name = prefix + "-" + counter.incrementAndGet();
    return new Thread( new Runnable() {
      public void run()
      {
        if ( setup != null) setup.run();
        runnable.run();
      }
    }, name);
  }
  
  private static AtomicInteger counter = new AtomicInteger( 0);
  
  private String prefix;
  private Runnable setup;
}