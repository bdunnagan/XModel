package org.xmodel.lss;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BNode<K>
{
  public static class Entry<K>
  {
    public Entry( K key, long value)
    {
      this.key = key;
      this.value = value;
    }
    
    public K key;
    public long value;
  }
  
  public BNode( BTree<K> tree, int minKeys, int maxKeys, long pointer, int count, Comparator<K> comparator)
  {
    this.tree = tree;
    this.minKeys = minKeys;
    this.maxKeys = maxKeys;
    this.pointer = pointer;
    this.count = count;
    this.entries = new ArrayList<Entry<K>>( count);
    this.children = new ArrayList<BNode<K>>( count);
  }
  
  public BNode( BNode<K> parent, List<Entry<K>> entries, List<BNode<K>> children)
  {
    this.tree = parent.tree;
    this.minKeys = parent.minKeys;
    this.maxKeys = parent.maxKeys;
    this.comparator = parent.comparator;
    this.entries = new ArrayList<Entry<K>>( entries);
    this.children = new ArrayList<BNode<K>>( children);
  }
    
  public int count()
  {
    return count;
  }
  
  public long insert( K key, long value)
  {
    if ( entries == null) load();
    
    if ( count() < maxKeys) split();
    
    int i = search( key);
    if ( i >= 0)
    {
      Entry<K> entry = entries.get( i);
      long old = entry.value;
      entry.value = value;
      return old;
    }
    else
    {
      Entry<K> entry = new Entry<K>( key, value);
      entries.add( i, entry);
      count++;
      return 0;
    }
  }
  
  public long delete( K key)
  {
    if ( entries == null) load();
    
    int i = search( key);
    return delete( key, i);
  }
  
  protected long delete( K key, int i)
  {
    if ( children.size() == 0)
    {
      // Case 1: Key is in leaf node
      if ( i < 0) return 0;
      count--;
      return entries.remove( i).value;
    }
    else
    {
      // Case 2: Key is in internal node
      if ( i >= 0)
      {
        BNode<K> less = children.get( i);
        if ( less.count() > minKeys)
        {
          // Case 2a: Lesser subtree can give up a key
          Entry<K> entry = entries.get( i);
          Entry<K> lessEntry = less.entries.get( less.entries.size());
          entries.set( i, lessEntry);
          less.delete( lessEntry.key, less.entries.size() - 1);
          return entry.value;
        }
        else
        {
          BNode<K> more = children.get( i+1);
          if ( more.count() > minKeys)
          {
            // Case 2b: Greater subtree can give up a key
            Entry<K> entry = entries.get( i);
            Entry<K> moreEntry = more.entries.get( 0);
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
            BNode<K> merged = merge( i);
            return merged.delete( key);
          }
        }
      }
      else
      {
        int k = -i + 1;
        
        // Case 3: Key not found in current node
        int j = biggestChild();
        BNode<K> node = children.get( j);
        if ( node.count() == minKeys)
        {
          // Case 3b: No children can give up a key
          BNode<K> merged = merge( k);
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
  
  public long get( K key)
  {
    if ( entries == null) load();
    
    int i = search( key);
    return (i >= 0)? entries.get( i).value: null;
  }
  
  protected void split()
  {
    int n = entries.size();
    int m = n / 2;
    BNode<K> less = new BNode<K>( this, entries.subList( 0, m), children.subList( 0, m+1));
    BNode<K> more = new BNode<K>( this, entries.subList( m+1, n), children.subList( m+1, n+1));
    Entry<K> entry = entries.get( m);
        
    entries.clear();
    children.clear();
    
    entries.add( entry);
    children.add( less);
    children.add( more);
  }
  
  protected BNode<K> merge( int i)
  {
    Entry<K> entry = entries.remove( i);
    
    BNode<K> less = children.get( i);
    BNode<K> more = children.remove( i+1);
    
    
    BNode<K> merged = less;
    merged.entries.add( entry);
    merged.entries.addAll( more.entries);
    merged.children.addAll( more.children);
    
    children.set( i, merged);
    
    return merged;
  }
  
  protected void push( int i, BNode<K> node)
  {
    
  }
  
  protected void pull( BNode<K> node, int i)
  {
  }
  
  protected int search( K key)
  {
    Entry<K> entry = new Entry<K>( key, 0);
    return Collections.binarySearch( entries, entry, entryComparator);
  }
  
  protected int biggestChild()
  {
    int maxCount = 0;
    int maxIndex = -1;
    for( int i=0; i<children.size(); i++)
    {
      BNode<K> child = children.get( i);
      if ( maxCount < child.count())
      {
        maxCount = child.count();
        maxIndex = i;
      }
    }
    return maxIndex;
  }

  private Comparator<Entry<K>> entryComparator = new Comparator<Entry<K>>() {
    public int compare( Entry<K> lhs, Entry<K> rhs)
    {
      return comparator.compare( lhs.key, rhs.key);
    }
  };
  
  protected void load()
  {
    tree.store.seek( pointer);
    
    count = tree.store.readInt();
    
    entries = new ArrayList<Entry<K>>( count);
    for( int i=0; i<=count; i++)
    {
      long pointer = tree.store.readLong();
      int count = tree.store.readInt();
      BNode<K> child = new BNode<K>( tree, minKeys, maxKeys, pointer, count, comparator);
      children.add( child);
     
      if ( i < count)
      {
        K key = tree.store.readKey();
        pointer = tree.store.readLong();
        Entry<K> entry = new Entry<K>( key, pointer);
        entries.add( entry);
      }
    }
  }
  
  protected void appendTo()
  {
    tree.store.seekEnd();

    tree.store.writeInt( count);
    
    for( int i=0; i<=count; i++)
    {
      BNode<K> child = children.get( i);
      tree.store.writeLong( child.pointer);
      tree.store.writeInt( child.count);

      if ( i < count)
      {
        Entry<K> entry = entries.get( i);
        tree.store.writeKey( entry.key);
        tree.store.writeLong( entry.value);
      }
    }
  }
  
  private BTree<K> tree;
  
  private int minKeys;
  private int maxKeys;
  private int count;
  private long pointer;
  
  private List<Entry<K>> entries;
  private List<BNode<K>> children;
  private Comparator<K> comparator;
}
