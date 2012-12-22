package org.xmodel.lss;

public class BTreeNode
{
  public BTreeNode( BTree tree)
  {
    this.tree = tree;
  }
  
  /**
   * @return Returns the number of keys in this node.
   */
  public int size()
  {
  }
  
  /**
   * @return Returns true if this node is a leaf.
   */
  public boolean isLeaf()
  {
  }
  
  /**
   * Insert a key into this node.
   * @param key The key.
   * @param pointer The pointer.
   * @return Returns 0 or the previous pointer.
   */
  public long insert( byte[] key, long pointer)
  {
  }
  
  /**
   * Split this node.
   * @return Returns the newly allocated node.
   */
  public BTreeNode split()
  {
  }
  
  /**
   * Delete a key from this node.
   * @param offset The offset of the key to be deleted.
   * @return Returns 0 or the previous pointer associated with the key.
   */
  public long delete( int offset)
  {
    if ( isLeaf())
    {
    }
    else
    {
      // Case 2c
    }
  }
  
  /**
   * Replace the record at the specified target offset with the record from the specified
   * source offset in the specified node.
   * @param targetOffset The offset of the record to be replaced.
   * @param node The node containing the record.
   * @param sourceOffset The offset of the source record.
   */
  public void replace( int targetOffset, BTreeNode node, int sourceOffset)
  {
  }

  /**
   * Returns the pointer associated with the key at the specified offset.
   * @param offset The offset of the key.
   * @return Returns the record pointer.
   */
  public long getPointer( int offset)
  {
  }
  
  /**
   * Find the offset of the specified key.
   * @param key The key.
   * @return Returns the offset of the key.
   */
  public int find( byte[] key)
  {
    return 0;
  }

  /**
   * Returns the child node containing keys that are less than the key at the specified offset.
   * @param offset The offset of the key, as returned by findOffset().
   * @return Returns the child.
   */
  public BTreeNode lessThan( int offset)
  {
  }
  
  /**
   * Returns the child node containing keys that are greater than the key at the specified offset.
   * @param offset The offset of the key, as returned by findOffset().
   * @return Returns the child.
   */
  public BTreeNode greaterThan( int offset)
  {
  }
  
  private BTree tree;
}
