package org.xmodel.storage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.xmodel.log.SLog;

public class Statistics
{
  public static void increment( IStorageClass storageClass)
  {
    counts.putIfAbsent( storageClass.getClass(), )
    
    counts.get( storageClass.getClass()).incrementAndGet();
    dump();
  }
  
  public static void decrement( IStorageClass storageClass)
  {
    counts.get( storageClass.getClass()).decrementAndGet();
    dump();
  }
  
  public static void dump()
  {
    long time = System.currentTimeMillis();
    if ( time > timestamp.get()) 
    {
      timestamp.set( time + 60000);
      
      StringBuilder sb = new StringBuilder();
      sb.append( "Storage class statistics: \n");
      for( Map.Entry<Class<?>, AtomicInteger> entry: counts.entrySet())
      {
        sb.append( String.format( "%s: %d\n", entry.getKey().getSimpleName(), entry.getValue().get()));
      }
      
      SLog.info( Statistics.class, sb.toString());
    }
  }
  
  private static ConcurrentHashMap<Class<?>, AtomicInteger> counts = new ConcurrentHashMap<Class<?>, AtomicInteger>();
  private static AtomicLong timestamp = new AtomicLong();
  
  // static
  {
    counts.put( ValueStorageClass.class, new AtomicInteger());
    counts.put( SmallDataStorageClass.class, new AtomicInteger());
    counts.put( SmallDataCachingPolicyStorageClass.class, new AtomicInteger());
    counts.put( DataStorageClass.class, new AtomicInteger());
    counts.put( DataAndCachingPolicyStorageClass.class, new AtomicInteger());
    counts.put( ModelListenerStorageClass.class, new AtomicInteger());
    counts.put( ModelListenerAndCachingPolicyStorageClass.class, new AtomicInteger());
    counts.put( PathListenerStorageClass.class, new AtomicInteger());
    counts.put( PathListenerAndCachingPolicyStorageClass.class, new AtomicInteger());
  }
}
