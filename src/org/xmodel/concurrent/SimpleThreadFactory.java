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
    this.threadCounter = new AtomicInteger( 0);
    this.prefix = prefix;
    this.setup = setup;
  }

  /* (non-Javadoc)
   * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
   */
  @Override
  public Thread newThread( final Runnable runnable)
  {
    String name = String.format( "%s-%d-%d", prefix, classCounter.incrementAndGet(), threadCounter.incrementAndGet());
    return new Thread( new Runnable() {
      public void run()
      {
        if ( setup != null) setup.run();
        runnable.run();
      }
    }, name);
  }
  
  private static AtomicInteger classCounter = new AtomicInteger( 0);
  
  private AtomicInteger threadCounter;
  private String prefix;
  private Runnable setup;
}
