package org.xmodel.lss;

/**
 * An abstract implementation of the b-tree algorithm.
 */
public class BTree
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
  public long insert( byte[] key, long pointer)
  {
    BTreeNode node = root;
    if ( root.size() == limit) node = root.split();
    return node.insert( key, pointer);
  }
  
  /**
   * Delete a key.
   * @param key The key.
   * @return Returns the pointer to the deleted record.
   */
  public long delete( byte[] key)
  {
    long pointer = delete( root, root.find( key), key);
    if ( root.size() == 0) root = root.greaterThan( 0);
    return pointer;
  }
  
  /**
   * Delete a key.
   * @param node The node containing the key.
   * @param offset The offset of the key, or -1 if the key was not found.
   * @param key The key.
   * @return Returns the pointer associated with the key.
   */
  protected long delete( BTreeNode node, int offset, byte[] key)
  {
    if ( node.isLeaf())
    {
      // Case 1: Key is in leaf node
      return node.delete( offset);
    }
    else
    {
      // Case 2: Key is in internal node
      if ( offset >= 0)
      {
        BTreeNode lesserSubtree = node.lessThan( offset);
        if ( lesserSubtree.size() >= degree)
        {
          // Case 2a: Lower subtree can give up a key
          node.replace( offset, lesserSubtree, lesserSubtree.size() - 1);
          return delete( lesserSubtree, lesserSubtree.size() - 1, key);
        }
        else
        {
          BTreeNode greaterSubtree = node.greaterThan( offset);
          if ( greaterSubtree.size() >= degree)
          {
            // Case 2b: Higher subtree can give up a key
            node.replace( offset, greaterSubtree, 0);
            return delete( greaterSubtree, 0, key);
          }
          else
          {
            // Case 2c: Neither subtree can give up a key
            //   Merge lower and higher subtrees
            //   Insert key into merged node
            //   Recursively delete key from merged node
            BTreeNode merged = node.merge( offset);
            // TODO: calculate key offset during merge
            return delete( merged, merged.find( key), key);
          }
        }
      }
      else
      {
        // Case 3: Key not found in current node
        BTreeNode shrinkable = node.findShrinkableChild();
        offset = -offset - 1;
        if ( shrinkable == null)
        {
          // Case 3b: No children can give up a key
          BTreeNode merged = node.merge( offset);
          // TODO: calculate key offset during merge
          return delete( merged, merged.find( key), key);
        }
        else
        {
          // Case 3a: There is a child that can give up a key
          BTreeNode next = node.greaterThan( offset);
          node.pull( shrinkable, 0);
          node.push( offset, next);
          // TODO: calculate key offset during rotate
          return delete( next, next.find( key), key);
        }
      }
    }
  }
  
  /**
   * Find the pointer for the specified key.
   * @param key The key.
   * @return Returns the pointer, or 0 if the key is not found.
   */
  public long find( byte[] key)
  {
    long pointer = 0;
    
    BTreeNode node = root;
    while( node != null)
    {
      pointer = node.find( key);
      node = getNode( pointer);
    }
    
    return pointer;
  }
  
  /**
   * Returns the BTreeNode at the specified pointer in the random-access store, or null
   * if the pointer does not point to a node.
   * @param pointer The pointer.
   * @return Returns null or the node.
   */
  protected BTreeNode getNode( long pointer)
  {
  }
 
  
  protected IRandomAccessStore store;
  private BTreeNode root;
  private int degree;
  private int limit;
}
