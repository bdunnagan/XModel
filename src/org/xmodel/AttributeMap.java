/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel;

import java.util.*;

/**
 * An implementation of the map interface for use with implementations of IModelObject. 
 */
public class AttributeMap implements Map<String, Object>
{
  /* (non-Javadoc)
   * @see java.util.Map#size()
   */
  public int size()
  {
    return (entries == null)? 0: entries.length;
  }

  /* (non-Javadoc)
   * @see java.util.Map#isEmpty()
   */
  public boolean isEmpty()
  {
    return (entries == null)? true: (entries.length == 0);
  }

  /* (non-Javadoc)
   * @see java.util.Map#containsKey(java.lang.Object)
   */
  public boolean containsKey( Object key)
  {
    if ( entries == null) return false;
    for( Map.Entry<String, Object> entry: entries)
      if ( entry.getKey().equals( key))
        return true;
    return false;
  }

  /* (non-Javadoc)
   * @see java.util.Map#containsValue(java.lang.Object)
   */
  public boolean containsValue( Object value)
  {
    if ( entries == null) return false;
    if ( value == null)
    {
      for( Map.Entry<String, Object> entry: entries)
        if ( entry.getValue() == null)
          return true;
    }
    else
    {
      for( Map.Entry<String, Object> entry: entries)
      {
        Object entryValue = entry.getValue();
        if ( entryValue != null && entryValue.equals( value))
          return true;
      }
    }
    return false;
  }
  
  /**
   * Returns the Entry for the specified key.
   * @param key The key.
   * @return Returns the Entry for the specified key.
   */
  private Map.Entry<String, Object> getEntry( Object key)
  {
    if ( entries == null) return null;
    if ( key == null)
    {
      for( Map.Entry<String, Object> entry: entries)
        if ( entry.getKey() == null)
          return entry;
    }
    else
    {
      for( Map.Entry<String, Object> entry: entries)
      {
        String entryKey = entry.getKey();
        if ( entryKey != null && entryKey.equals( key))
          return entry;
      }
    }
    return null;
  }

  /* (non-Javadoc)
   * @see java.util.Map#get(java.lang.Object)
   */
  public Object get( Object key)
  {
    Map.Entry<String, Object> entry = getEntry( key);
    return (entry == null)? null: entry.getValue();
  }

  /**
   * Add an entry.
   * @param entry The entry.
   */
  private void addEntry( Entry entry)
  {
    if ( entries == null)
    {
      entries = new Entry[ 1];
      entries[ 0] = entry;
    }
    else
    {
      Entry[] array = new Entry[ entries.length+1];
      System.arraycopy( entries, 0, array, 0, entries.length);
      entries = array;
      entries[ entries.length-1] = entry;
    }
  }
  
  /* (non-Javadoc)
   * @see java.util.Map#put(K, V)
   */
  public Object put( String key, Object value)
  {
    Map.Entry<String,Object> entry = getEntry( key);
    if ( entry == null)
    {
      addEntry( new Entry( key, value));
      return null;
    }
    else
    {
      return entry.setValue( value);
    }
  }

  /* (non-Javadoc)
   * @see java.util.Map#remove(java.lang.Object)
   */
  public Object remove( Object key)
  {
    if ( entries == null) return null;
    Map.Entry<String, Object> entry = null;
    if ( key == null)
    {
      for( int i=0; i<entries.length; i++)
      {
        entry = entries[ i];
        if ( entry.getKey() == null)
        {
          Entry[] array = new Entry[ entries.length-1];
          if ( array.length > 0) System.arraycopy( entries, 0, array, 0, array.length);
          entries = array;
          break;
        }
      }
    }
    else
    {
      for( int i=0; i<entries.length; i++)
      {
        entry = entries[ i];
        String entryKey = entry.getKey();
        if ( entryKey != null && entryKey.equals( key))
        {
          Entry[] array = new Entry[ entries.length-1];
          if ( array.length > 0) 
          {
            System.arraycopy( entries, 0, array, 0, i);
            System.arraycopy( entries, i+1, array, i, array.length - i);
          }
          entries = array;
          break;
        }
      }
    }
    return entry;
  }

  /* (non-Javadoc)
   * @see java.util.Map#putAll(java.util.Map)
   */
  public void putAll( Map<? extends String, ? extends Object> t)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see java.util.Map#clear()
   */
  public void clear()
  {
    entries = null;
  }

  /* (non-Javadoc)
   * @see java.util.Map#keySet()
   */
  public Set<String> keySet()
  {
    if ( entries == null) return Collections.emptySet();
    Set<String> result = new HashSet<String>( entries.length);
    for( Map.Entry<String, Object> entry: entries) result.add( entry.getKey());
    return result;
  }

  /* (non-Javadoc)
   * @see java.util.Map#values()
   */
  public Collection<Object> values()
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see java.util.Map#entrySet()
   */
  public Set<Map.Entry<String, Object>> entrySet()
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    StringBuffer string = new StringBuffer();
    for( Entry entry: entries)
    {
      string.append( entry.getKey());
      string.append( " [");
      string.append( entry.getValue());
      string.append( "]\n");
    }
    return string.toString();
  }

  private static class Entry implements Map.Entry<String, Object>
  {
    public Entry( String key, Object value)
    {
      this.key = key.intern();
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
    public Object getValue()
    {
      return value;
    }

    /* (non-Javadoc)
     * @see java.util.Map.Entry#setValue(V)
     */
    public Object setValue( Object value)
    {
      Object old = this.value;
      this.value = value;
      return old;
    }
    
    String key;
    Object value;
  }
  
  private Entry[] entries;
}
