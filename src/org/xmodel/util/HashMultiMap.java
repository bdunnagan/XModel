/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * HashMultiMap.java
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

import java.util.*;

/**
  This interface is similar to a Map except that it allows multiple entries
  for a single key.
*/
public class HashMultiMap<K, T> implements MultiMap<K, T>
{
  public HashMultiMap()
  {
    map = new LinkedHashMap<K, List<T>>();
  }

  public HashMultiMap( int initialCapacity)
  {
    map = new LinkedHashMap<K, List<T>>( initialCapacity);
  }

  public HashMultiMap( int initialCapacity, float loadFactor)
  {
    map = new LinkedHashMap<K, List<T>>( initialCapacity, loadFactor);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.util.MultiMap#clear()
   */
  public void clear()
  {
    map.clear();
    size = 0;
  }

  /* (non-Javadoc)
   * @see org.xmodel.util.MultiMap#containsKey(Object)
   */
  public boolean containsKey( Object key)
  {
    return map.containsKey( key);
  }

  /* (non-Javadoc)
   * @see org.xmodel.util.MultiMap#containsValue(Object)
   */
  public boolean containsValue( Object value)
  {
    Iterator<List<T>> iter = map.values().iterator();
    while ( iter.hasNext())
    {
      List<T> list = iter.next();
      if ( list.contains( value)) return true;
    }
    return false;
  }

  /* (non-Javadoc)
   * @see org.xmodel.util.MultiMap#get(K)
   */
  public List<T> get( K key)
  {
    List<T> values = map.get( key);
    if ( values == null) return null;
    return Collections.unmodifiableList( values);
  }

  /* (non-Javadoc)
   * @see org.xmodel.util.MultiMap#getFirst(null)
   */
  public T getFirst( K key)
  {
    List<T> list = map.get( key);
    return (list == null)? null: list.get( 0);
  }

  /* (non-Javadoc)
   * @see org.xmodel.util.MultiMap#iterator(null)
   */
  public Iterator<T> iterator( K key)
  {
    return getOrCreateList( key).listIterator();
  }

  /* (non-Javadoc)
   * @see org.xmodel.util.MultiMap#isEmpty()
   */
  public boolean isEmpty()
  {
    return map.isEmpty();
  }

  /* (non-Javadoc)
   * @see org.xmodel.util.MultiMap#keySet()
   */
  public Set<K> keySet()
  {
    return map.keySet();
  }

  /* (non-Javadoc)
   * @see org.xmodel.util.MultiMap#put(null, null)
   */
  public void put( K key, T value)
  {
    List<T> list = getOrCreateList( key);
    list.add( value);
    size++;
  }

  /* (non-Javadoc)
   * @see org.xmodel.util.MultiMap#putAll(K, java.util.Collection)
   */
  public void putAll( K key, Collection<T> values)
  {
    List<T> list = getOrCreateList( key);
    list.addAll( values);
    size += values.size();
  }

  /* (non-Javadoc)
   * @see org.xmodel.util.MultiMap#putAll(org.xmodel.util.MultiMap)
   */
  public void putAll( MultiMap<K, T> map)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.util.MultiMap#remove(null, null)
   */
  public boolean remove( K key, T value)
  {
    List<T> list = map.get( key);
    if ( list == null) return false;
    if ( list.remove( value))
    {
      if ( list.size() == 0) map.remove( key);
      size--;
      return true;
    }
    return false;
  }

  /* (non-Javadoc)
   * @see org.xmodel.util.MultiMap#removeAll(null)
   */
  public List<T> removeAll( K key)
  {
    List<T> list = map.remove( key);
    if ( list != null) size -= list.size();
    return list;
  }

  /* (non-Javadoc)
   * @see org.xmodel.util.MultiMap#size(null)
   */
  public int size( K key)
  {
    List<T> list = map.get( key);
    return (list == null)? 0: list.size();
  }

  /* (non-Javadoc)
   * @see org.xmodel.util.MultiMap#size()
   */
  public int size()
  {
    return size;
  }

  /* (non-Javadoc)
   * @see org.xmodel.util.MultiMap#values()
   */
  public List<T> values()
  {
    List<T> list = new ArrayList<T>( 1);
    Iterator<List<T>> iter = map.values().iterator();
    while ( iter.hasNext()) list.addAll( iter.next());
    return list;
  }

  /**
    Get or create if necessary the List for a key.
    @param key The key.
    @return Returns the List associated with this key.
  */
  private List<T> getOrCreateList( K key)
  {
    List<T> list = (List<T>)map.get( key);
    if ( list == null)
    {
      list = new ArrayList<T>( 1);
      map.put( key, list);
    }
    return list;
  }

  // attributes
  Map<K, List<T>> map;
  int size;
};
