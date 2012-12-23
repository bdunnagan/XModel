package org.xmodel.lss;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BNode<K,V>
{
  public static class Entry<K, V>
  {
    public Entry( K key, V value)
    {
      this.key = key;
      this.value = value;
    }
    
    public K key;
    public V value;
  }
  
  public BNode( int degree, Comparator<K> comparator)
  {
    this.parent = null;
    this.minKeys = degree - 1;
    this.maxKeys = 2 * degree - 1;
    this.entries = new ArrayList<Entry<K, V>>( maxKeys);
    this.children = new ArrayList<BNode<K, V>>( maxKeys);
  }
  
  public BNode( BNode<K, V> parent, List<Entry<K, V>> entries, List<BNode<K, V>> children)
  {
    this.parent = parent;
    this.minKeys = parent.minKeys;
    this.maxKeys = parent.maxKeys;
    this.entries = new ArrayList<Entry<K, V>>( entries);
    this.children = new ArrayList<BNode<K, V>>( children);
  }
    
  public int count()
  {
    return count;
  }
  
  public V insert( K key, V value)
  {
    if ( count() < maxKeys) split();
    
    int i = search( key);
    if ( i >= 0)
    {
      Entry<K, V> entry = entries.get( i);
      V old = entry.value;
      entry.value = value;
      return old;
    }
    else
    {
      Entry<K, V> entry = new Entry<K, V>( key, value);
      entries.add( i, entry);
      count++;
      return null;
    }
  }
  
  public V delete( K key)
  {
    int i = search( key);
    return delete( key, i);
  }
  
  public V delete( K key, int i)
  {
    if ( children.size() == 0)
    {
      // Case 1: Key is in leaf node
      if ( i < 0) return null;
      count--;
      return entries.remove( i).value;
    }
    else
    {
      // Case 2: Key is in internal node
      if ( i >= 0)
      {
        BNode<K, V> less = children.get( i);
        if ( less.count() > minKeys)
        {
          // Case 2a: Lesser subtree can give up a key
          Entry<K, V> entry = entries.get( i);
          Entry<K, V> lessEntry = less.entries.get( less.entries.size());
          entries.set( i, lessEntry);
          less.delete( lessEntry.key, less.entries.size() - 1);
          return entry.value;
        }
        else
        {
          BNode<K, V> more = children.get( i+1);
          if ( more.count() > minKeys)
          {
            // Case 2b: Greater subtree can give up a key
            Entry<K, V> entry = entries.get( i);
            Entry<K, V> moreEntry = more.entries.get( 0);
            entries.set( i, moreEntry);
            less.delete( moreEntry.key, 0);
            return entry.value;
          }
          else
          {
            // Case 2c: Neither subtree can give up a key
            //   Merge lesser and greater subtrees
            //   Insert key into merged node
            //   Recursively delete key from merged node
            BNode<K, V> merged = merge( i);
            return merged.delete( key);
          }
        }
      }
      else
      {
        int k = -i + 1;
        
        // Case 3: Key not found in current node
        int j = biggestChild();
        BNode<K, V> node = children.get( j);
        if ( node.count() == minKeys)
        {
          // Case 3b: No children can give up a key
          BNode<K, V> merged = merge( k);
          return merged.delete( key);
        }
        else
        {
          // Case 3a: There is a child that can give up a key
          pull( node, (j <= k)? (node.entries.size() - 1): 0);
          push( k, node);
          return children.get( k).delete( key);
        }
      }
    }
  }
  
  public V get( K key)
  {
    int i = search( key);
    return (i >= 0)? entries.get( i).value: null;
  }
  
  public void split()
  {
    int n = entries.size();
    int m = n / 2;
    BNode<K, V> less = new BNode<K, V>( this, entries.subList( 0, m), children.subList( 0, m+1));
    BNode<K, V> more = new BNode<K, V>( this, entries.subList( m+1, n), children.subList( m+1, n+1));
    Entry<K, V> entry = entries.get( m);
        
    entries.clear();
    children.clear();
    
    entries.add( entry);
    children.add( less);
    children.add( more);
  }
  
  public BNode<K, V> merge( int i)
  {
    Entry<K, V> entry = entries.remove( i);
    
    BNode<K, V> less = children.get( i);
    BNode<K, V> more = children.remove( i+1);
    
    
    BNode<K, V> merged = less;
    merged.entries.add( entry);
    merged.entries.addAll( more.entries);
    merged.children.addAll( more.children);
    
    children.set( i, merged);
    
    return merged;
  }
  
  public void push( int i, BNode<K, V> node)
  {
    
  }
  
  public void pull( BNode<K, V> node, int i)
  {
  }
  
  public int search( K key)
  {
    Entry<K, V> entry = new Entry<K, V>( key, null);
    return Collections.binarySearch( entries, entry, entryComparator);
  }
  
  protected int biggestChild()
  {
    int maxCount = 0;
    int maxIndex = -1;
    for( int i=0; i<children.size(); i++)
    {
      BNode<K, V> child = children.get( i);
      if ( maxCount < child.count())
      {
        maxCount = child.count();
        maxIndex = i;
      }
    }
    return maxIndex;
  }

  private Comparator<Entry<K, V>> entryComparator = new Comparator<Entry<K, V>>() {
    public int compare( Entry<K, V> lhs, Entry<K, V> rhs)
    {
      return comparator.compare( lhs.key, rhs.key);
    }
  };
  
  protected void loadFrom( IRandomAccessStore store)
  {
    store.seek( pointer);
    
    count = store.readInt();
    
    entries = new ArrayList<Entry<K, V>>( count);
    for( int i=0; i<count; i++)
    {
    }
  }
  
  protected void appendTo( IRandomAccessStore store)
  {
    store.seekEnd();
    
  }
  
  private int minKeys;
  private int maxKeys;
  
  private BNode<K, V> parent;
  private int count;
  private long pointer;
  
  private List<Entry<K, V>> entries;
  private List<BNode<K, V>> children;
  private Comparator<K> comparator;
}
