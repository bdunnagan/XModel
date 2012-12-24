package org.xmodel.lss;

import java.util.ArrayList;
import java.util.Collection;
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
    this.comparator = comparator;
    this.pointer = pointer;
    this.count = count;
    this.entries = new ArrayList<Entry<K>>( count);
    this.children = new ArrayList<BNode<K>>( count);
    this.dirty = true;
  }
  
  public BNode( BNode<K> parent, List<Entry<K>> entries, List<BNode<K>> children)
  {
    this.tree = parent.tree;
    this.minKeys = parent.minKeys;
    this.maxKeys = parent.maxKeys;
    this.comparator = parent.comparator;
    this.entries = new ArrayList<Entry<K>>( entries);
    this.children = new ArrayList<BNode<K>>( children);
    this.count = entries.size();
    this.dirty = true;
  }
      
  public int count()
  {
    return count;
  }
  
  public long insert( K key, long value)
  {
    if ( entries == null) load();
    
    int i = search( key);
    if ( i >= 0)
    {
      dirty = true;
      Entry<K> entry = entries.get( i);
      long old = entry.value;
      entry.value = value;
      return old;
    }
    else
    {
      int k = -i - 1;
      
      if ( count < maxKeys)
      {
        if ( children.size() == 0)
        {
          dirty = true;
          Entry<K> entry = new Entry<K>( key, value);
          addEntry( k, entry);
          return 0;
        }
        else
        {
          BNode<K> node = children.get( k);
          if ( node.count() < maxKeys)
          {
            return node.insert( key, value);
          }
          else
          {
            // Split internal node (*)
            dirty = true;
            node.split();

            // move median key and its branches into this node
            Entry<K> median = node.entries.get( 0);
            BNode<K> less = node.children.get( 0);
            BNode<K> more = node.children.get( 1);
            addEntry( k, median);
            children.set( k, less);
            children.add( k+1, more);
            
            // discard unused node
            node.markGarbage();

            // insert into appropriate subtree
            int c = comparator.compare( key, median.key);
            if ( c < 0) return less.insert( key, value);
            if ( c > 0) return more.insert( key, value);
            
            long old = median.value;
            median.value = value;
            return old;
          }
        }
      }
      else
      {
        // Split root (*)
        dirty = true;
        split();
        return insert( key, value);
      }
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
    if ( entries == null) load();
    
    if ( children.size() == 0)
    {
      // Case 1: Key is in leaf node
      if ( i < 0) return 0;
      dirty = true;
      return removeEntry( i).value;
    }
    else
    {
      // Case 2: Key is in internal node
      if ( i >= 0)
      {
        dirty = true;
        BNode<K> less = children.get( i);
        if ( less.count() > minKeys)
        {
          // Case 2a: Lesser subtree can give up a key
          less.dirty = true;
          Entry<K> entry = entries.get( i);
          Entry<K> lessEntry = less.entries.get( less.count() - 1);
          entries.set( i, lessEntry);
          less.delete( lessEntry.key, less.count() - 1);
          return entry.value;
        }
        else
        {
          BNode<K> more = children.get( i+1);
          if ( more.count() > minKeys)
          {
            // Case 2b: Greater subtree can give up a key
            more.dirty = true;
            Entry<K> entry = entries.get( i);
            Entry<K> moreEntry = more.entries.get( 0);
            entries.set( i, moreEntry);
            more.delete( moreEntry.key, 0);
            return entry.value;
          }
          else
          {
            // Case 2c: Neither subtree can give up a key
            //   Merge lesser and greater subtrees
            //   Insert key into merged node
            //   Recursively delete key from merged node
            dirty = true;
            BNode<K> merged = merge( i);
            return merged.delete( key);
          }
        }
      }
      else
      {
        // Case 3: Key not found in current node
        int k = -i - 1;
        BNode<K> node = children.get( k);
        if ( node.count() > minKeys)
        {
          return node.delete( key);
        }
        else
        {
          BNode<K> less = (k > 0)? children.get( k-1): null;
          if ( less != null && less.count() > minKeys)
          {
            // Case 3a: There is a child that can give up a key
            rightRotate( k-1);
            return node.delete( key);
          }
          else
          {
            BNode<K> more = (k < count)? children.get( k+1): null;
            if ( more != null && more.count() > minKeys)
            {
              // Case 3a: There is a child that can give up a key
              leftRotate( k);
              return node.delete( key);
            }
            else
            {
              // 
              // Merging here is always performed by choosing the key in the parent that follows
              // the child being deleted, which means that the child will be merged with its greater
              // sibling - with the exception of the greatest key in this node.
              //
              if ( k == entries.size()) k--;
              
              // Case 3b: No children can give up a key
              BNode<K> merged = merge( k);
              return merged.delete( key);
            }
          }
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

    BNode<K> less = null;
    BNode<K> more = null;
    
    if ( children.size() == 0)
    {
      less = new BNode<K>( this, entries.subList( 0, m), Collections.<BNode<K>>emptyList());
      more = new BNode<K>( this, entries.subList( m+1, n), Collections.<BNode<K>>emptyList());
    }
    else
    {
      less = new BNode<K>( this, entries.subList( 0, m), children.subList( 0, m+1));
      more = new BNode<K>( this, entries.subList( m+1, n), children.subList( m+1, n+1));
    }
    
    Entry<K> median = entries.get( m);
        
    clearEntries();
    children.clear();
    
    addEntry( median);
    children.add( less);
    children.add( more);
  }
  
  protected BNode<K> merge( int i)
  {
    Entry<K> entry = removeEntry( i);
    
    BNode<K> less = children.get( i);
    BNode<K> more = children.remove( i+1);
    more.markGarbage();
    
    less.dirty = true;
    BNode<K> merged = less;
    merged.addEntry( entry);
    merged.addAllEntries( more.entries);
    merged.children.addAll( more.children);
    
    children.set( i, merged);
    
    return merged;
  }
  
  protected void leftRotate( int i)
  {
    BNode<K> less = children.get( i);
    BNode<K> more = children.get( i+1);
    
    this.dirty = true;
    less.dirty = true;
    more.dirty = true;
    
    // move key in this node to end of lesser child
    less.addEntry( removeEntry( i));
    
    // move least child of greater child to least child
    if ( more.children.size() > 0)
      less.children.add( more.children.remove( 0));
    
    // move least key of greater child to this node
    addEntry( i, more.removeEntry( 0));
  }
  
  protected void rightRotate( int i)
  {
    BNode<K> less = children.get( i);
    BNode<K> more = children.get( i+1);
    
    this.dirty = true;
    less.dirty = true;
    more.dirty = true;
    
    // move key in this node to start of greater child
    more.addEntry( 0, removeEntry( i));
    
    // move greater child of lesser child to greater child
    if ( less.children.size() > 0)
      more.children.add( 0, less.children.remove( less.count()));
    
    // move least key of greater child to this node
    addEntry( i, less.removeEntry( less.count() - 1));
  }
  
  protected int search( K key)
  {
    Entry<K> entry = new Entry<K>( key, 0);
    return Collections.binarySearch( entries, entry, entryComparator);
  }
  
  protected void addEntry( Entry<K> entry)
  {
    entries.add( entry);
    count++;
  }
  
  protected void addEntry( int i, Entry<K> entry)
  {
    count++;
    entries.add( i, entry);
  }
  
  protected void addAllEntries( Collection<Entry<K>> entries)
  {
    count += entries.size();
    this.entries.addAll( entries);
  }
  
  protected Entry<K> removeEntry( int i)
  {
    count--;
    return entries.remove( i);
  }
  
  protected void clearEntries()
  {
    count = 0;
    entries.clear();
  }
  
  /**
   * @return Returns the list of children.
   */
  protected List<BNode<K>> children()
  {
    return children;
  }
  
  private Comparator<Entry<K>> entryComparator = new Comparator<Entry<K>>() {
    public int compare( Entry<K> lhs, Entry<K> rhs)
    {
      return comparator.compare( lhs.key, rhs.key);
    }
  };
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    return toString( "");
  }
  
  /**
   * Create string representation with the specified indentation.
   * @param indent The indentation.
   * @return Returns the string.
   */
  public String toString( String indent)
  {
    StringBuilder sb = new StringBuilder();
    for( int i=0; i<count; i++)
    {
      if ( children.size() > 0)
      {
        BNode<K> less = children.get( i);
        sb.append( less.toString( indent + "   |")); sb.append( '\n');
      }
      
      Entry<K> entry = entries.get( i);
      sb.append( indent); sb.append( "-"); sb.append( entry.key); sb.append( '\n');
    }
    
    if ( children.size() > 0)
    {
      BNode<K> more = children.get( count);
      sb.append( more.toString( indent + "   |")); sb.append( '\n');
    }
    
    if ( sb.length() > 0 && sb.charAt( sb.length() - 1) == '\n') sb.setLength( sb.length() - 1);
    
    return sb.toString();
  }    

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
        addEntry( entry);
      }
    }
  }
  
  public void store()
  {
    if ( !dirty) return;
    
    IRandomAccessStore<K> store = tree.store;
    
    store.seekEnd();
    pointer = store.position();

    store.writeInt( count);
    
    for( int i=0; i<=count; i++)
    {
      BNode<K> child = children.get( i);
      if ( child.dirty) child.store();
      store.writeLong( child.pointer);
      store.writeInt( child.count);

      if ( i < count)
      {
        Entry<K> entry = entries.get( i);
        store.writeKey( entry.key);
        store.writeLong( entry.value);
      }
    }
  }
  
  protected void markGarbage()
  {
    if ( pointer == 0) return;
    
    tree.store.seek( pointer);
    tree.store.writeInt( 0);
    
    pointer = 0;
  }
  
  private BTree<K> tree;
  
  private int minKeys;
  private int maxKeys;
  private int count;
  private long pointer;
  private boolean dirty;
  
  private List<Entry<K>> entries;
  private List<BNode<K>> children;
  private Comparator<K> comparator;
}
