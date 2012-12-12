package org.xmodel.concurrent;

import org.xmodel.log.SLog;

/**
 * Dispatcher execution statistics.
 */
public class Statistics
{
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
  public synchronized void executionFinished()
  {
    finished = System.nanoTime();
    long elapsed = (finished - started);
    totalTime += elapsed;
    if ( elapsed > maxTime) maxTime = totalTime;
    count++;
    
    SLog.info( this, this);
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
    
    if ( days > 0) return String.format( "%d days, %d hours, %d mins", days, hours, mins);
    if ( hours > 0) return String.format( "%d hours, %d mins, %d secs", hours, mins, secs);
    if ( mins > 0) return String.format( "%d mins, %d secs, %1.3f msecs", mins, secs, msecs);
    if ( secs > 0) return String.format( "%d secs, %1.3f msecs", secs, msecs, usecs);
    return String.format( "%1.3f msecs", msecs);
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    return String.format( "Start: %1$tb %1$td %1$tY %1$tT, Duration: %2$s, Average: %3$s, Max: %4$s, Count: %5$d", 
        firstTime, 
        formatDuration( totalTime), 
        formatDuration( totalTime / count),
        formatDuration( maxTime),
        count);
  }

  private long firstTime;
  private long started;
  private long finished;
  private long totalTime;
  private long maxTime;
  private int count;
}
