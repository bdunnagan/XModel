package org.xmodel.concurrent;

import org.xmodel.log.Log;

/**
 * Dispatcher execution statistics.
 */
public class Statistics
{
  public Statistics( Log log)
  {
    this.log = log;
    this.bins = new int[ 7];
  }
  
  /**
   * Notify that task execution has started.
   */
  public synchronized void executionStarted()
  {
    started = System.nanoTime();
    
    if ( firstTime == 0) 
    {
      firstTime = System.currentTimeMillis();
    }
  }
  
  /**
   * Notify that task execution has ended.
   */
  public void executionFinished()
  {
    synchronized( this)
    {
      finished = System.nanoTime();
      long elapsed = finished - started;
      totalTime += elapsed;
      count++;
      
      elapsed /= 1e6;
      if ( elapsed < 10) bins[ 0]++;
      else if ( elapsed < 100) bins[ 1]++;
      else if ( elapsed < 500) bins[ 2]++;
      else if ( elapsed < 1000) bins[ 3]++;
      else if ( elapsed < 5000) bins[ 4]++;
      else if ( elapsed < 10000) bins[ 5]++;
      else bins[ 6]++;
    }
    
    log.verbose( this);
  }
  
  /**
   * Format a duration.
   * @param duration The duration in nanoseconds.
   * @return Returns the formatted duration.
   */
  private String formatDuration( long duration)
  {
    double usecs = duration / 1000.0;
    double msecs = (usecs / 1000.0);
    int secs = (int)(msecs / 1000);
    int mins = secs / 60;
    int hours = mins / 60;
    int days = hours / 24;

    usecs -= (int)msecs * 1000;
    msecs -= secs * 1000;
    secs -= mins * 60;
    mins -= hours * 60;
    hours -= days * 24;
    
    if ( days > 0) return String.format( "%dd, %dh, %dm", days, hours, mins);
    if ( hours > 0) return String.format( "%dh, %dm, %ds", hours, mins, secs);
    if ( mins > 0) return String.format( "%dm, %ds, %1.3fms", mins, secs, msecs);
    if ( secs > 0) return String.format( "%ds, %1.3fms", secs, msecs, usecs);
    return String.format( "%1.3fms", msecs);
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    long now = System.currentTimeMillis();
    return String.format( "%d, %1.1f%%, Last=%s, Sum=%s, Avg=%s, 0ms [%d] 10ms [%d] 100ms [%d] 500ms [%d] 1s [%d] 5s [%d] 10s [ %d]", 
        count,
        totalTime / 1e4 / (now - firstTime),
        formatDuration( finished - started), 
        formatDuration( totalTime), 
        formatDuration( totalTime / count),
        bins[ 0], bins[ 1], bins[ 2], bins[ 3], bins[ 4], bins[ 5], bins[ 6]);
  }

  private Log log;
  private long firstTime;
  private long started;
  private long finished;
  private long totalTime;
  private int count;
  private int[] bins;
}
