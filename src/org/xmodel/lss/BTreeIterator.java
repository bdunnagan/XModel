package org.xmodel.lss;

import java.io.IOException;
import org.xmodel.lss.BNode.Entry;

/**
 * A BTree iterator that can move forward or backward through the ordered keys in the tree.
 */
class BTreeIterator<K>
{
  /**
   * Create a cursor positioned at the specified entry.
   * @param node The node.
   * @param offset The offset of the entry.
   */
  BTreeIterator( BNode<K> node, int offset)
  {
    this( null, node, offset);
  }
  
  /**
   * Create a cursor positioned at the specified entry with the specified parent.
   * @param parent The cursor for the parent node.
   * @param node The node.
   * @param offset The offset of the entry.
   */
  BTreeIterator( BTreeIterator<K> parent, BNode<K> node, int offset)
  {
    this.parent = parent;
    this.node = node;
    this.offset = offset;
  }
  
  /**
   * @return Returns the current entry.
   */
  public Entry<K> get()
  {
    if ( offset < 0 || offset == node.count()) return null;
    return node.entries.get( offset);
  }
  
  /**
   * @return Returns true if there is a key that is less than the current key.
   */
  public boolean hasPrevious()
  {
    return offset >= 0;
  }
  
  /**
   * Unlike the Iterator interface, cursors are created pointing at an entry.  Therefore, the
   * current entry should be consumed before calling <code>previous()</code> or <code>next()</code>.
   * @return Returns a cursor that is positioned on the previous entry.
   */
  public BTreeIterator<K> previous() throws IOException
  {
    BTreeIterator<K> leaf = previousLeaf();
    if ( leaf != null) return leaf;
    
    if ( offset == 0)
    {
      BTreeIterator<K> parent = previousParent();
      if ( parent != null) return parent;
    }
    
    offset--;
    return this;
  }
  
  /**
   * When the cursor points to the first entry in the node, a call to <code>previous()</code> will
   * unwind the stack by visiting each parent node until it finds a parent that has a previous entry.
   * @return Returns null or a cursor that is positioned on the previous parent.
   */
  private BTreeIterator<K> previousParent()
  {
    BTreeIterator<K> cursor = parent;
    while( cursor != null && cursor.offset == 0)
      cursor = cursor.parent;
    if ( cursor != null) cursor.offset--;
    return cursor;
  }
  
  /**
   * The <code>previous()</code> method is required to return a cursor that is positioned at a leaf
   * node unless the stack has just been unwound and an entry of an internal node is the current entry.
   * @return Returns null or a cursor that is positioned on the previous leaf.
   */
  private BTreeIterator<K> previousLeaf() throws IOException
  {
    if ( node.getChildren().size() == 0) return null;
    
    BNode<K> child = node.getChildren().get( offset);
    BTreeIterator<K> cursor = new BTreeIterator<K>( this, child, child.count());
    
    while( child.getChildren().size() > 0)
    {
      child = child.children.get( child.count());
      cursor = new BTreeIterator<K>( cursor, child, child.count());
    }

    if ( cursor != null) cursor.offset--;
    return cursor;
  }
  
  /**
   * @return Returns true if there is a key that is greater than the current key.
   */
  public boolean hasNext()
  {
    return offset < node.count();
  }
  
  /**
   * Unlike the Iterator interface, cursors are created pointing at an entry.  Therefore, the
   * current entry should be consumed before calling <code>previous()</code> or <code>next()</code>.
   * @return Returns a cursor that is positioned on the next entry.
   */
  public BTreeIterator<K> next() throws IOException
  {
    offset++;
    
    BTreeIterator<K> leaf = nextLeaf();
    if ( leaf != null) return leaf;
    
    if ( offset == node.count())
    {
      BTreeIterator<K> parent = nextParent();
      if ( parent != null) return parent;
    }
    
    return this;
  }
  
  /**
   * When the cursor points to the last entry in the node, a call to <code>next()</code> will
   * unwind the stack by visiting each parent node until it finds a parent that has a next entry.
   * @return Returns null or a cursor that is positioned on the next parent.
   */
  private BTreeIterator<K> nextParent()
  {
    BTreeIterator<K> cursor = parent;
    while( cursor != null && cursor.offset == cursor.node.count())
      cursor = cursor.parent;
    return cursor;
  }
  
  /**
   * The <code>next()</code> method is required to return a cursor that is positioned at a leaf
   * node unless the stack has just been unwound and an entry of an internal node is the current entry.
   * @return Returns null or a cursor that is positioned on the next leaf.
   */
  private BTreeIterator<K> nextLeaf() throws IOException
  {
    if ( node.getChildren().size() == 0) return null;
    
    BNode<K> child = node.getChildren().get( offset);
    BTreeIterator<K> cursor = new BTreeIterator<K>( this, child, 0);
    
    while( child.getChildren().size() > 0)
    {
      child = child.children.get( 0);
      cursor = new BTreeIterator<K>( cursor, child, 0);
    }
    
    return cursor;
  }
  
  public BTreeIterator<K> parent;
  public BNode<K> node;
  public int offset;
}