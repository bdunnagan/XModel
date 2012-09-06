package org.xmodel.concurrent;

import java.util.ArrayList;
import java.util.List;

class DutyCycle
{
  public static synchronized DutyCycle getInstance()
  {
    DutyCycle instance = tlocal.get();
    if ( instance == null)
    {
      instance = new DutyCycle();
      instance.name = Thread.currentThread().getName();
      tlocal.set( instance);
      instances.add( instance);
    }
    return instance;
  }
  
  public static synchronized void reset()
  {
    tlocal = new ThreadLocal<DutyCycle>();
    instances = new ArrayList<DutyCycle>();
  }
  
  public void start()
  {
    start = System.nanoTime();
  }
  
  public void stop()
  {
    stop = System.nanoTime();
    
    if ( last > 0)
    {
      ++cycles;
      if ( stop > last) total += (stop - start) / (double)(stop - last);
    }
    
    last = stop;
  }
  
  public double get()
  {
    return (cycles == 0)? 0: total / cycles;
  }
  
  public static void dump()
  {
    for( DutyCycle instance: instances)
      System.out.printf( "%s: %1.1f\n", instance.name, instance.get() * 100);
  }
  
  private static ThreadLocal<DutyCycle> tlocal = new ThreadLocal<DutyCycle>();
  private static List<DutyCycle> instances = new ArrayList<DutyCycle>();

  private String name;
  private double total;
  private int cycles;
  private long last;
  private long start;
  private long stop;
}