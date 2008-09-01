/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.util;

import java.util.*;

/**
 * An implementation of Map which associates a value with the set of String keys which begin with
 * the prefix to which the value is mapped.
 */
public class PartialKeyMap<T> implements Map<String, T>
{
  /* (non-Javadoc)
   * @see java.util.Map#clear()
   */
  public void clear()
  {
    entries = null;
  }

  /* (non-Javadoc)
   * @see java.util.Map#containsKey(java.lang.Object)
   */
  public boolean containsKey( Object key)
  {
    return findKey( key.toString()) != -1;
  }

  /* (non-Javadoc)
   * @see java.util.Map#containsValue(java.lang.Object)
   */
  public boolean containsValue( Object value)
  {
    if ( entries != null)
    {
      for( Map.Entry<String, T> entry: entries)
      {
        Object test = entry.getValue();
        if ( test == null && value == null) return true;
        if ( test != null && value != null && test.equals( value)) return true;
      }
    }
    return false;
  }

  /* (non-Javadoc)
   * @see java.util.Map#entrySet()
   */
  public Set<java.util.Map.Entry<String, T>> entrySet()
  {
    Set<Map.Entry<String,T>> set = new HashSet<Map.Entry<String,T>>();
    set.addAll( entries);
    return set;
  }

  /* (non-Javadoc)
   * @see java.util.Map#get(java.lang.Object)
   */
  public T get( Object key)
  {
    int index = findKey( key);
    return (index < 0)? null: entries.get( index).getValue();
  }

  /* (non-Javadoc)
   * @see java.util.Map#isEmpty()
   */
  public boolean isEmpty()
  {
    return entries == null;
  }

  /* (non-Javadoc)
   * @see java.util.Map#keySet()
   */
  public Set<String> keySet()
  {
    if ( entries == null) return Collections.emptySet();
    Set<String> set = new HashSet<String>();
    for( Map.Entry<String, T> entry: entries) set.add( entry.getKey());
    return set;
  }

  /* (non-Javadoc)
   * @see java.util.Map#put(java.lang.Object, java.lang.Object)
   */
  public T put( String key, T value)
  {
    if ( entries == null) entries = new ArrayList<Map.Entry<String, T>>();
    
    int index = findExactKey( key);
    if ( index < 0)
    {
      Map.Entry<String, T> entry = new Entry<String, T>( key, value);
      entries.add( entry);
      return null;
    }
    else
    {
      Map.Entry<String, T> entry = new Entry<String, T>( key, value);
      return entries.set( index, entry).getValue();
    }
  }

  /* (non-Javadoc)
   * @see java.util.Map#putAll(java.util.Map)
   */
  public void putAll( Map<? extends String, ? extends T> map)
  {
    if ( entries == null) entries = new ArrayList<Map.Entry<String, T>>();
    
    for( Map.Entry<? extends String, ? extends T> entry: map.entrySet())
      entries.add( new Entry<String, T>( entry.getKey(), entry.getValue()));
  }

  /* (non-Javadoc)
   * @see java.util.Map#remove(java.lang.Object)
   */
  public T remove( Object key)
  {
    int index = findExactKey( key);
    if ( index < 0) return null;
    return entries.remove( index).getValue();
  }

  /* (non-Javadoc)
   * @see java.util.Map#size()
   */
  public int size()
  {
    return (entries != null)? entries.size(): 0;
  }

  /* (non-Javadoc)
   * @see java.util.Map#values()
   */
  public Collection<T> values()
  {
    if ( entries == null) return Collections.emptySet();
    Set<T> set = new HashSet<T>();
    for( Map.Entry<String, T> entry: entries) set.add( entry.getValue());
    return set;
  }

  /**
   * Find the longest prefix which matches the specified key.
   * @param key The key.
   * @return Returns the index of the matching key.
   */
  private int findKey( Object key)
  {
    int matchIndex = -1;
    int matchLength = 0;
    String test = (key != null)? key.toString(): null;
    if ( entries != null)
    {
      for( int i=0; i<entries.size(); i++)
      {
        String entryKey = entries.get( i).getKey();
        if ( test == null && entryKey == null) return i;
        if ( test != null && entryKey != null && test.startsWith( entryKey)) 
        {
          if ( matchLength < entryKey.length())
          {
            matchIndex = i;
            matchLength = entryKey.length();
          }
        }
      }
    }
    return matchIndex;
  }

  /**
   * Find the specified key.
   * @param key The key.
   * @return Returns the index of the key.
   */
  private int findExactKey( Object key)
  {
    String test = (key != null)? key.toString(): null;
    if ( entries != null)
    {
      for( int i=0; i<entries.size(); i++)
      {
        String entryKey = entries.get( i).getKey();
        if ( test == null && entryKey == null) return i;
        if ( test != null && entryKey != null && test.equals( entryKey)) return i;
      }
    }
    return -1;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    if (entries != null)
    {
        for( Map.Entry<String, T> entry: entries)
        {
            builder.append( entry.getKey());
            builder.append( " = ");
            builder.append( entry.getValue());
            builder.append( "\n");
        }
    }
    return builder.toString();
  }

  /**
   * Implementation of Entry.
   */
  private static class Entry<K, T> implements Map.Entry<String, T>
  {
    public Entry( String key, T value)
    {
      this.key = key;
      this.value = value;
    }
    
    /* (non-Javadoc)
     * @see java.util.Map.Entry#getKey()
     */
    public String getKey()
    {
      return key;
    }

    /* (non-Javadoc)
     * @see java.util.Map.Entry#getValue()
     */
    public T getValue()
    {
      return value;
    }

    /* (non-Javadoc)
     * @see java.util.Map.Entry#setValue(java.lang.Object)
     */
    public T setValue( T value)
    {
      T previous = this.value;
      this.value = value;
      return previous;
    }
    
    private String key;
    private T value;
  }
  
  private List<Map.Entry<String, T>> entries;
}
