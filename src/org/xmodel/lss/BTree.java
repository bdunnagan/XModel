package org.xmodel.lss;

import java.util.Comparator;

/**
 * A B+Tree implementation that supports arbitrary keys and uses an instance of IRandomAccessStore to load and store nodes.
 */
public class BTree<K>
{
  public BTree( IRandomAccessStore<K> store, Comparator<K> comparator)
  {
    this.store = store;
    store.seek( 0);
    int degree = store.readInt();
    int minKeys = degree - 1;
    int maxKeys = 2 * degree - 1;
    long pointer = store.readLong();
    int count = store.readInt();
    root = new BNode<K>( this, minKeys, maxKeys, pointer, count, comparator);
  }
  
  public BTree( int degree, IRandomAccessStore<K> store, Comparator<K> comparator)
  {
    this.store = store;
    int minKeys = degree - 1;
    int maxKeys = 2 * degree - 1;
    root = new BNode<K>( this, minKeys, maxKeys, 0, 0, comparator);
  }
  
  /**
   * Insert a record.
   * @param key The key.
   * @param pointer The pointer.
   * @return Returns 0 or the previous record associated with the key.
   */
  public long insert( K key, long pointer)
  {
    return root.insert( key, pointer);
  }
  
  /**
   * Delete a record.
   * @param key The key.
   * @return Returns 0 or the pointer associated with the key.
   */
  public long delete( K key)
  {
    return root.delete( key);
  }
  
  /**
   * Returns the pointer associated with the specified key.
   * @param key The key.
   * @return Returns 0 or the pointer associated with the specified key.
   */
  public long get( K key)
  {
    return root.get( key);
  }
 
  protected IRandomAccessStore<K> store;
  private BNode<K> root;
}
