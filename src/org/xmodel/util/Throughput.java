package org.xmodel.util;

/**
 * Utility class to measure the rate of an event.
 */
public class Throughput
{
  /**
   * Create a throughput measure with the specified window of events.
   * @param size The number of events in the window.
   */
  public Throughput( int size)
  {
    window = new long[ size];
  }
  
  /**
   * Mark the completion of an event.
   */
  public synchronized void event()
  {
    window[ index++] = System.nanoTime();
    if ( length < window.length) length++;
    if ( index == window.length) index = 0;
  }

  /**
   * @return Returns the throughput in events per second.
   */
  public synchronized float get()
  {
    if ( length < 2) return 0;
    
    if ( length < window.length)
    {
      float elapsed = (System.nanoTime() - window[ 0]) / 1e9f;
      if ( elapsed < 1) elapsed = 1;
      return length / elapsed;
    }
    else
    {
      float elapsed = (System.nanoTime() - window[ index]) / 1e9f;
      return length / elapsed;
    }
  }
  
  private long[] window;
  private int index;
  private int length;
}
