package org.xmodel.storage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.xmodel.log.SLog;

public class Statistics
{
  public synchronized static void increment( IStorageClass storageClass)
  {
    AtomicInteger count = counts.get( storageClass.getClass());
    if ( count == null)
    {
      count = new AtomicInteger();
      counts.put( storageClass.getClass(), count);
    }
    
    count.incrementAndGet();
    dump();
  }
  
  public synchronized static void decrement( IStorageClass storageClass)
  {
    AtomicInteger count = counts.get( storageClass.getClass());
    if ( count == null)
    {
      count = new AtomicInteger();
      counts.put( storageClass.getClass(), count);
    }
    
    count.incrementAndGet();
    dump();
  }
  
  private static void dump()
  {
    long time = System.currentTimeMillis();
    if ( time > timestamp.get()) 
    {
      timestamp.set( time + 10000);
      
      StringBuilder sb = new StringBuilder();
      sb.append( "Storage class statistics: \n");
      for( Map.Entry<Class<?>, AtomicInteger> entry: counts.entrySet())
      {
        sb.append( String.format( "%s: %d\n", entry.getKey().getSimpleName(), entry.getValue().get()));
      }
      
      SLog.info( Statistics.class, sb.toString());
    }
  }
  
  private static Map<Class<?>, AtomicInteger> counts = new HashMap<Class<?>, AtomicInteger>();
  private static AtomicLong timestamp = new AtomicLong();
}
