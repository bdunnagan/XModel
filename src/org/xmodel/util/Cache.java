package org.xmodel.util;

import java.util.LinkedHashMap;

public abstract class Cache<K, V>
{
  protected Cache( int maxSize)
  {
    cache = new CacheImpl<K, V>( maxSize);
  }
  
  public V fetch( K key, Object arg1, Object arg2, Object arg3)
  {
    V value = cache.get( key);
    if ( value == null)
    {
      value = miss( key, arg1, arg2, arg3);
      cache.put( key, value);
    }
    return value;
  }

  protected abstract V miss( K key, Object arg1, Object arg2, Object arg3);
  
  @SuppressWarnings("serial")
  private static class CacheImpl<K, V> extends LinkedHashMap<K, V>
  {
    public CacheImpl( int maxSize)
    {
      this.maxSize = maxSize;
    }
    
    @Override
    protected final boolean removeEldestEntry( java.util.Map.Entry<K, V> eldest)
    {
      return size() > maxSize;
    }
    
    private int maxSize;
  }
  
  private CacheImpl<K, V> cache;
}
