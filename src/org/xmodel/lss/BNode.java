package org.xmodel.lss;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BNode<K>
{
  /**
   * A BNode consists of an ordered list of Entry objects that associate a key with a pointer into the IRandomAccessStore.
   */
  public static class Entry<K> implements Comparable<Entry<K>>
  {
    public Entry( K key, long value)
    {
      this.key = key;
      this.value = value;
    }
    
    public K getKey()
    {
      return key;
    }
    
    public long getPointer()
    {
      return value;
    }
    
    @SuppressWarnings("unchecked")
    public int compareTo( Entry<K> entry)
    {
      return ((Comparable<K>)key).compareTo( entry.key);
    }
    
    private K key;
    private long value;
  }
    
  /**
   * Create a BTree root node.
   * @param tree The BTree.
   * @param minKeys The minimum number of keys in a node.
   * @param maxKeys The maximum number of keys in a node.
   * @param pointer The location of the node in the IRandomAccessStore.
   * @param count The number of entries in the node.
   * @param comparator The key comparator.
   */
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
    this.update = 0;
    this.storedUpdate = -1;
    this.loaded = true;
  }
  
  /**
   * Create an internal BTree node that has not yet been loaded from the store.
   * @param parent The parent node.
   * @param pointer The location of the node in the IRandomAccessStore.
   * @param count The number of entries in the node.
   */
  public BNode( BNode<K> parent, long pointer, int count)
  {
    this.tree = parent.tree;
    this.parent = parent;
    this.minKeys = parent.minKeys;
    this.maxKeys = parent.maxKeys;
    this.comparator = parent.comparator;
    this.pointer = pointer;
    this.count = count;
    this.entries = new ArrayList<Entry<K>>( count);
    this.children = new ArrayList<BNode<K>>( count);
    this.update = 0;
    this.storedUpdate = -1;
    this.loaded = false;
  }
  
  /**
   * Create an internal BTree node.
   * @param parent The parent of the node.
   * @param entries The entries.
   * @param children The children.
   */
  public BNode( BNode<K> parent, List<Entry<K>> entries, List<BNode<K>> children)
  {
    this.tree = parent.tree;
    this.parent = parent;
    this.minKeys = parent.minKeys;
    this.maxKeys = parent.maxKeys;
    this.comparator = parent.comparator;
    this.entries = new ArrayList<Entry<K>>( entries);
    this.children = new ArrayList<BNode<K>>( children);
    this.count = entries.size();
    this.update = 0;
    this.storedUpdate = -1;
    this.loaded = true;
  }
   
  /**
   * @return Returns null or the parent of the node.
   */
  public BNode<K> parent()
  {
    return parent;
  }
  
  /**
   * @return Returns the number of entries in the node.
   */
  public int count()
  {
    return count;
  }
  
  /**
   * Create an entry for the specified value under the specified key.
   * @param key The key.
   * @param value The value.
   * @return Returns 0 or the previous value associated with the key.
   */
  public long insert( K key, long value) throws IOException
  {
    if ( !loaded) load();
    
    int i = search( key);
    if ( i >= 0)
    {
      update++;
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
          update++;
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
            update++;
            node.split();

            // move median key and its branches into this node
            Entry<K> median = node.getEntries().get( 0);
            BNode<K> less = node.getChildren().get( 0);
            BNode<K> more = node.getChildren().get( 1);
            addEntry( k, median);
            children.set( k, less);
            children.add( k+1, more);
            
            // discard unused node
            tree.markGarbage( node);

            // insert into appropriate subtree
            @SuppressWarnings("unchecked")
            int c = (comparator != null)? comparator.compare( key, median.key): ((Comparable<K>)key).compareTo( median.key);
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
        update++;
        split();
        return insert( key, value);
      }
    }
  }
  
  /**
   * Delete the entry under the specified key.
   * @param key The key.
   * @return Returns 0 or the value that was associated with the key.
   */
  public long delete( K key) throws IOException
  {
    if ( !loaded) load();
    int i = search( key);
    return delete( key, i);
  }
  
  /**
   * Delete the entry under the specified key.
   * @param key The key.
   * @param i The index of the key or the index where the key would be inserted.
   * @return Returns 0 or the value that was associated with the key.
   */
  protected long delete( K key, int i) throws IOException
  {
    if ( !loaded) load();
    
    if ( children.size() == 0)
    {
      // Case 1: Key is in leaf node
      if ( i < 0) return 0;
      update++;
      return removeEntry( i).value;
    }
    else
    {
      // Case 2: Key is in internal node
      if ( i >= 0)
      {
        update++;
        BNode<K> less = children.get( i);
        if ( less.count() > minKeys)
        {
          // Case 2a: Lesser subtree can give up a key
          less.update++;
          Entry<K> entry = entries.get( i);
          Entry<K> lessEntry = less.getEntries().get( less.count() - 1);
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
            more.update++;
            Entry<K> entry = entries.get( i);
            Entry<K> moreEntry = more.getEntries().get( 0);
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
            update++;
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
  
  /**
   * Delete the specified entry.
   * @param key The key.
   * @param pointer The pointer.
   * @throws IOException
   */
  public void delete( K key, long pointer) throws IOException
  {
    // revisit after non-unique keys implemented
    delete( key);
  }
  
  /**
   * Get the value under the specified key.
   * @param key The key.
   * @return Returns 0 or the value under the specified key.
   */
  public long get( K key) throws IOException
  {
    if ( !loaded) load();
    
    int i = search( key);
    if ( i >= 0) return entries.get( i).value;
    
    if ( children.size() == 0) return 0;
    
    long pointer = children.get( -i - 1).get( key);
    if ( pointer != 0)
    {
      // record may be garbage if index was not written after delete
      if ( tree.store.isGarbage( pointer))
      {
        delete( key, pointer);
        return 0;
      }
    }      
    
    return pointer;
  }
  
  /**
   * Get a cursor for navigating keys in order.
   * @param key The starting key.
   * @return Returns a cursor.
   */
  public Cursor<K> getCursor( K key) throws IOException
  {
    return getCursor( null, key);
  }
  
  /**
   * Get a nested cursor for navigating keys in order.
   * @param cursor The parent node cursor.
   * @param key The starting key.
   * @return Returns a cursor.
   */
  protected Cursor<K> getCursor( Cursor<K> parent, K key) throws IOException
  {
    if ( !loaded) load();
    
    int i = search( key);
    if ( i >= 0) return new Cursor<K>( parent, this, i);
    
    if ( children.size() == 0) return null;
    return children.get( -i - 1).getCursor( new Cursor<K>( parent, this, -i - 1), key);
  }
  
  /**
   * Split this node such that all entries less than the median key and all entries greater than
   * the median key become new nodes that are children of this node.  Only the median key remains
   * in this node.
   */
  protected void split() throws IOException
  {
    if ( !loaded) load();
    
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
  
  /**
   * Merge the nodes on either side of the entry with the specified index.
   * @param i The index of the entry.
   * @return Returns the merge node.
   */
  protected BNode<K> merge( int i) throws IOException
  {
    Entry<K> entry = removeEntry( i);
    
    BNode<K> less = children.get( i);
    BNode<K> more = children.remove( i+1);
    tree.markGarbage( more);
    
    less.update++;
    BNode<K> merged = less;
    merged.addEntry( entry);
    merged.addAllEntries( more.getEntries());
    merged.children.addAll( more.getChildren());
    
    children.set( i, merged);
    
    return merged;
  }
  
  /**
   * Left rotate the tree at the entry with the specified index.
   * @param i The index of the entry.
   */
  protected void leftRotate( int i) throws IOException
  {
    BNode<K> less = children.get( i);
    BNode<K> more = children.get( i+1);
    
    this.update++;
    less.update++;
    more.update++;
    
    // move key in this node to end of lesser child
    less.addEntry( removeEntry( i));
    
    // move least child of greater child to least child
    if ( more.children.size() > 0)
      less.children.add( more.children.remove( 0));
    
    // move least key of greater child to this node
    addEntry( i, more.removeEntry( 0));
  }
  
  /**
   * Right rotate the tree at the entry with the specified index.
   * @param i The index of the entry.
   */
  protected void rightRotate( int i) throws IOException
  {
    BNode<K> less = children.get( i);
    BNode<K> more = children.get( i+1);
    
    this.update++;
    less.update++;
    more.update++;
    
    // move key in this node to start of greater child
    more.addEntry( 0, removeEntry( i));
    
    // move greater child of lesser child to greater child
    if ( less.children.size() > 0)
      more.children.add( 0, less.children.remove( less.count()));
    
    // move least key of greater child to this node
    addEntry( i, less.removeEntry( less.count() - 1));
  }
  
  /**
   * Perform a binary search of this node for the specified key.
   * @param key The key.
   * @return Returns the index of the key, or -insert - 1.
   */
  protected int search( K key) throws IOException
  {
    if ( !loaded) load();
    Entry<K> entry = new Entry<K>( key, 0);
    if ( comparator == null)
    {
      return Collections.binarySearch( entries, entry);
    }
    else
    {
      return Collections.binarySearch( entries, entry, entryComparator);
    }
  }
  
  /**
   * Add an entry to the node.
   * @param entry The entry.
   */
  protected void addEntry( Entry<K> entry) throws IOException
  {
    if ( !loaded) load();
    count++;
    entries.add( entry);
  }
  
  /**
   * Add an entry to the node at the specified index.
   * @param i The node where the entry will be inserted.
   * @param entry The entry.
   */
  protected void addEntry( int i, Entry<K> entry) throws IOException
  {
    if ( !loaded) load();
    count++;
    entries.add( i, entry);
  }
  
  /**
   * Add all of the specified entries to this node.
   * @param entries The entries.
   */
  protected void addAllEntries( Collection<Entry<K>> entries) throws IOException
  {
    if ( !loaded) load();
    count += entries.size();
    this.entries.addAll( entries);
  }
  
  /**
   * Remove an entry from this node.
   * @param i The index of the entry.
   * @return Returns the entry that was removed.
   */
  protected Entry<K> removeEntry( int i) throws IOException
  {
    if ( !loaded) load();
    count--;
    return entries.remove( i);
  }
  
  /**
   * Clear the entries in this node.
   */
  protected void clearEntries()
  {
    count = 0;
    if ( entries != null) entries.clear();
  }
  
  /**
   * Load this node from the IRandomAccessStore, if necessary, and return its entries.
   * @return Returns the entries in this node.
   */
  protected List<Entry<K>> getEntries() throws IOException
  {
    if ( !loaded) load();
    return entries;
  }
  
  /**
   * Add a child to this node.
   * @param node The child.
   */
  protected void addChild( BNode<K> node)
  {
    children.add( node);
  }
  
  /**
   * Load this node from the IRandomAccessStore, if necessary, and return its children.
   * There is always exactly one more child than there are entries.
   * @return Returns the children in this node.
   */
  protected List<BNode<K>> getChildren() throws IOException
  {
    if ( !loaded) load();
    return children;
  }
  
  private Comparator<Entry<K>> entryComparator = new Comparator<Entry<K>>() {
    public int compare( Entry<K> lhs, Entry<K> rhs)
    {
      return comparator.compare( lhs.key, rhs.key);
    }
  };

  /**
   * Set the pointer to this node in the store.
   * @param pointer The pointer.
   */
  public void setPointer( long pointer)
  {
    this.pointer = pointer;
  }
  
  /**
   * @return Returns 0 or the pointer to this node in the store.
   */
  public long getPointer()
  {
    return pointer;
  }
  
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
    if ( !loaded) return "[not loaded]";
    
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

  /**
   * Load this node from the IRandomAccessStore.
   */
  protected void load() throws IOException
  {
    loaded = true;
    tree.store.readNode( this);
  }
  
  /**
   * Write this node to the IRandomAccessStore.
   * @return Returns true if the node was dirty and required storing.
   */
  public boolean store() throws IOException
  {
    boolean dirty = storedUpdate != update;
    
    if ( children.size() > 0)
      for( int i=0; i<=count; i++)
        if ( children.get( i).store())
          dirty = true;
    
    if ( dirty)
    {  
      long oldPointer = pointer;
      tree.store.writeNode( this);
      if ( oldPointer != 0) 
        tree.store.markGarbage( oldPointer);
      storedUpdate = update;
    }
    
    return dirty;
  }
    
  protected BTree<K> tree;
  private BNode<K> parent;
  
  private int minKeys;
  private int maxKeys;
  private int count;
  protected long pointer;
  
  protected boolean loaded;
  protected long update;
  private long storedUpdate;
  
  protected List<Entry<K>> entries;
  protected List<BNode<K>> children;
  private Comparator<K> comparator;
}
