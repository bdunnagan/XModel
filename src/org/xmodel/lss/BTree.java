package org.xmodel.lss;

/**
 * An abstract implementation of the b-tree algorithm.
 */
public class BTree<K, V>
{
  public BTree( IRandomAccessStore store, int degree)
  {
    this.store = store;
    this.degree = degree;
    this.limit = 2 * degree - 1;
  }
  
  /**
   * Insert a pointer into the tree.
   * @param key The key.
   * @param pointer The pointer.
   * @return Returns 0 or the previous record associated with the key.
   */
  public long insert( K key, V pointer)
  {
  }
 
  protected IRandomAccessStore store;
  private BNode<K, V> root;
}
