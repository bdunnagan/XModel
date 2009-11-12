/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * NanoTimer.java
 * 
 * Copyright 2009 Robert Arvin Dunnagan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.xmodel.util;

public class NanoTimer
{
  public NanoTimer()
  {
    if ( avg == 0) measure( this);
    minSum = 0;
    avgSum = 0;
    loop = 0;
    last = 0;
    count = 0;
  }
  
  /**
   * Create a timer which will print stats for the specified object periodically.
   * @param period The period in microseconds.
   */
  public NanoTimer( long period)
  {
    this();
    this.period = period * 1000;
  }
  
  /**
   * Create a timer which will print stats for the specified object periodically.
   * @param prefix The prefix string used by toString().
   * @param period The period in microseconds.
   */
  public NanoTimer( String prefix, long period)
  {
    this();
    this.prefix = prefix;
    this.period = period * 1000;
  }
  
  /**
   * Start the stopwatch.
   */
  public void start()
  {
    t0 = System.nanoTime();
  }

  /**
   * Stop the stopwatch and return the compensated elapsed time in nanoseconds.
   * @return Returns the compensated elapsed time in nanoseconds.
   */
  public long stop()
  {
    long t1 = System.nanoTime();
    long e = (t1 - t0);
    last = (e - (long)avg);
    avgSum += last;
    minSum += (e - min);
    loop += last;
    count++;
    if ( period > 0 && loop > period)
    {
      loop = 0;
      System.out.println( toString());
    }
    return e - (int)avg;
  }
  
  /**
   * Returns the minimum total execution time.
   * @return Returns the minimum total execution time.
   */
  public long minimum()
  {
    return minSum;
  }
  
  /**
   * Returns the estimated total execution time.
   * @return Returns the estimated total execution time.
   */
  public long estimated()
  {
    return (long)avgSum;
  }

  /**
   * Returns a formatted string representing the state of the timer.
   * @param total The total elapsed time.
   * @param last The last elapsed time.
   * @param count The number of trials.
   * @return Returns the formatted string.
   */
  protected String toString( String total, String last, int count)
  {
    StringBuilder builder = new StringBuilder();
    builder.append( (prefix == null)? "timer: ": prefix);
    builder.append( '['); builder.append( count); builder.append( "] "); 
    builder.append( last);
    builder.append( " ("); builder.append( total); builder.append( ')');
    return builder.toString();
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    format( builder, avgSum);
    String s1 = builder.toString();
    
    builder.setLength( 0);
    format( builder, last);
    String s2 = builder.toString();
    
    return toString( s1, s2, count);
  }
  
  /**
   * Format time.
   * @param builder The string destination.
   * @param time The time in nanoseconds.
   * @return Returns the formatted time.
   */
  static public void format( StringBuilder builder, float time)
  {
    if ( time < 10000)
    {
      builder.append( time);
      builder.append( "ns");
    }
    else if ( time < 1000000)
    {
      builder.append( time / 1000f);
      builder.append( "us");
    }
    else if ( time < 1000000000)
    {
      builder.append( time / 1000000f);
      builder.append( "ms");
    }
    else
    {
      builder.append( time / 1000000000f);
      builder.append( "s");
    }
  }

  /**
   * Measure the compensation required for the specified timer.
   * @param time The timer.
   */
  static private void measure( NanoTimer time)
  {
    min = Long.MAX_VALUE;
    long sum = 0;
    int count = 200000;
    for( int i=0; i<count; i++)
    {
      time.start();
      long e = time.stop();
      if ( e < min) min = e;
      sum += e;
    }
    avg = sum / count;
  }

  long t0;
  long last;
  long minSum;
  long avgSum;
  Object object;
  long period;
  long loop;
  int count;
  String prefix;
  static long min;
  static long avg;
  
  public static void main( String[] args)
  {
    NanoTimer time = new NanoTimer( 1);
    for( int i=0; i<100000; i++)
    {
      time.start();
      time.stop();
    }
  }
}
