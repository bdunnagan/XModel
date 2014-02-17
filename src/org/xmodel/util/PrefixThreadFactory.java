package org.xmodel.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class PrefixThreadFactory implements ThreadFactory
{
  public PrefixThreadFactory( String prefix)
  {
    this.prefix = prefix;
    this.threadPriority = Thread.NORM_PRIORITY;
  }
  
  public PrefixThreadFactory( String prefix, int threadPriority)
  {
    this.prefix = prefix;
    this.threadPriority = threadPriority;
  }
  
  public Thread newThread( Runnable runnable)
  {
    Thread thread = new Thread( runnable, prefix + "-" + counter.getAndIncrement());
    thread.setDaemon( true);
    thread.setPriority( threadPriority);
    return thread;
  }
  
  private String prefix;
  private int threadPriority;
  private AtomicInteger counter = new AtomicInteger( 1);
};
