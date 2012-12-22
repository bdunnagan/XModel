package org.xmodel.lss;

class BTreeNode
{
  public BTreeNode( BTree tree, long pointer)
  {
    this.tree = tree;
    this.pointer = pointer;
    
    tree.store.seek( pointer);
    int header = tree.store.readInt();
    this.isLeaf = (header > 0);
    this.size = header & 0x7FFFFFFF;
  }
  
  /**
   * @return Returns the number of keys in this node.
   */
  int size()
  {
    return size;
  }
  
  /**
   * @return Returns true if this node is a leaf.
   */
  boolean isLeaf()
  {
    return isLeaf;
  }
  
  /**
   * Insert a key into this node.
   * @param key The key.
   * @param pointer The pointer.
   * @return Returns 0 or the previous pointer.
   */
  long insert( byte[] key, long pointer)
  {
  }
  
  /**
   * Split this node.
   * @return Returns the newly allocated node.
   */
  BTreeNode split()
  {
  }
  
  /**
   * Delete a key from this node.
   * @param offset The offset of the key to be deleted.
   * @return Returns 0 or the previous pointer associated with the key.
   */
  long delete( int offset)
  {
  }
  
  /**
   * Replace the record at the specified target offset with the record from the specified
   * source offset in the specified node.
   * @param targetOffset The offset of the record to be replaced.
   * @param node The node containing the record.
   * @param sourceOffset The offset of the source record.
   */
  void replace( int targetOffset, BTreeNode node, int sourceOffset)
  {
  }

  /**
   * Merge the left and right subtrees of the key at the specified offset and move the key into merged node.
   * @param offset The offset of the key.
   * @return Returns the merge node.
   */
  BTreeNode merge( int offset)
  {
  }
  
  /**
   * Set the predecessor of the key with the specified offset.
   * @param offset The offset of the key, or the end of the list.
   * @param node The node.
   */
  void setPredecessor( int offset, BTreeNode node)
  {
  }

  /**
   * @return Returns the first child that has at least degree keys.
   */
  BTreeNode findShrinkableChild()
  {
  }
  
  /**
   * Move a key from this node into a child node.
   * @param offset The offset of the key.
   * @param node The node containing the key to be moved.
   */
  void push( int offset, BTreeNode node)
  {
  }

  /**
   * Move a key from a child node into this node.
   * @param node The node containing the key to be moved.
   * @param offset The offset of the key.
   */
  void pull( BTreeNode node, int offset)
  {
  }

  /**
   * Returns the key at the specified offset.
   * @param offset The offset of the key.
   * @return Returns the key.
   */
  byte[] getKey( int offset)
  {
  }

  /**
   * Returns the pointer associated with the key at the specified offset.
   * @param offset The offset of the key.
   * @return Returns the record pointer.
   */
  long getPointer( int offset)
  {
  }
  
  /**
   * Find the offset of the specified key, or return the offset of the next smallest key
   * converted into a negative number by negating and subtracting 1.  The offset of the
   * next smallest key is calculated by negating the result and subtracting 1.
   * @param key The key.
   * @return Returns the offset of the key, or a negative number indicating the next smallest key.
   */
  int find( byte[] key)
  {
    return 0;
  }
  
  /**
   * Returns the child node containing keys that are less than the key at the specified offset.
   * @param offset The offset of the key, as returned by findOffset().
   * @return Returns the child.
   */
  BTreeNode lessThan( int offset)
  {
  }
  
  /**
   * Returns the child node containing keys that are greater than the key at the specified offset.
   * @param offset The offset of the key, as returned by findOffset().
   * @return Returns the child.
   */
  BTreeNode greaterThan( int offset)
  {
  }

  private BTree tree;
  private long pointer;
  private int size;
  private boolean isLeaf;
}
