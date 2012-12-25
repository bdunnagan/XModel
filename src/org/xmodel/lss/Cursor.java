package org.xmodel.lss;

import java.util.Iterator;
import org.xmodel.lss.BNode.Entry;

class Cursor<K> implements Iterator<Entry<K>>
{
  Cursor( BNode<K> node, int offset, boolean ascending)
  {
    this.node = node;
    this.offset = offset;
    this.ascending = ascending;
    this.valid = node.update;
    if ( offset >= 0 && offset < node.count()) this.key = node.entries.get( offset).getKey();
  }
  
  /* (non-Javadoc)
   * @see java.util.ListIterator#hasNext()
   */
  @Override
  public boolean hasNext()
  {
    if ( child != null && child.hasNext()) return true;
    if ( valid != node.update) refresh();
    return ascending? offset < node.count(): offset >= 0;
  }

  /* (non-Javadoc)
   * @see java.util.ListIterator#next()
   */
  @Override
  public Entry<K> next()
  {
    if ( valid != node.update) refresh();

    if ( child != null)
    {
      Entry<K> entry = child.next();
      if ( entry != null) return entry;
      child = null;
    }
    
    if ( offset < 0 || offset >= node.count()) return null;
    Entry<K> entry = node.entries.get( offset);
    
    if ( node.children().size() > 0)
      nextLeaf( ascending? offset + 1: offset);
    
    if ( ascending) offset++; else offset--;
    
    return entry;
  }
  
  private void nextLeaf( int offset)
  {
    if ( ascending)
    {
      BNode<K> childNode = node.children.get( offset);
      child = new Cursor<K>( childNode, 0, ascending);
      if ( childNode.children.size() > 0) child.nextLeaf( 0);
    }
    else
    {
      BNode<K> childNode = node.children.get( offset);
      child = new Cursor<K>( childNode, childNode.count() - 1, ascending);
      if ( childNode.children.size() > 0) child.nextLeaf( childNode.count());
    }
  }
  
  private void refresh()
  {
    offset = node.search( key);
    if ( offset < 0)
    {
      offset = -offset - 1;
      if ( node.children.size() > 0)
        child = new Cursor<K>( node.children.get( offset), 0, ascending);
    }
  }

  /* (non-Javadoc)
   * @see java.util.ListIterator#remove()
   */
  @Override
  public void remove()
  {
    throw new UnsupportedOperationException();
  }

  BNode<K> node;
  K key;
  int offset;
  long valid;
  Cursor<K> child;
  boolean ascending;
}