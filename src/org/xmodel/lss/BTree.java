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
  }
  
  /**
   * Delete a key.
   * @param node The node containing the key.
   * @param offset The offset of the key, or -1 if the key was not found.
   */
  protected void delete( BTreeNode node, int offset)
  {
    if ( node.isLeaf())
    {
      // Case 1
      node.delete( offset);
    }
    else
    {
      if ( offset >= 0)
      {
        BTreeNode lower = node.lessThan( offset);
        if ( lower.size() >= degree)
        {
          // Case 2a
          node.replace( offset, lower, lower.size() - 1);
          delete( lower, lower.size() - 1);
        }
        else
        {
          BTreeNode higher = node.greaterThan( offset);
          if ( higher.size() >= degree)
          {
            // Case 2b
            node.replace( offset, higher, 0);
            delete( higher, 0);
          }
          else
          {
            // Case 2c
            node.delete( offset);
          }
        }
      }
      else
      {
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
 
  
  private IRandomAccessStore store;
  private BTreeNode root;
  private int degree;
  private int limit;
}
