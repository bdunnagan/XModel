package org.xmodel.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Thread-safe JDBC Statement cache.
 */
public class ThreadLocalMap<K, V>
{
  public ThreadLocalMap()
  {
    threadMaps = new ThreadLocal<Map<K, V>>();
  }
  
  /**
   * Get a statement from the cache.
   * @param key The key.
   * @return Returns null or the statement.
   */
  public V get( K key)
  {
    Map<K, V> cache = threadMaps.get();
    if ( cache == null)
    {
      cache = createThreadMap();
      threadMaps.set( cache);
    }
    
    return cache.get( key);
  }
  
  /**
   * Put a statement in the cache.
   * @param key The key.
   * @param statement The statement.
   */
  public void put( K key, V statement)
  {
    Map<K, V> cache = threadMaps.get();
    if ( cache == null)
    {
      cache = createThreadMap();
      threadMaps.set( cache);
    }
    
    cache.put( key, statement);
  }
  
  /**
   * Create a new Map instance for uninitialized thread local data.
   * @return Returns the Map instance.
   */
  protected Map<K, V> createThreadMap()
  {
    return new HashMap<K, V>();
  }

  private ThreadLocal<Map<K, V>> threadMaps;
}
