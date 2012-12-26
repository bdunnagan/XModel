package org.xmodel.lss;

import org.xmodel.lss.BNode.Entry;

class Cursor<K>
{
  Cursor( BNode<K> node, int offset)
  {
    this( null, node, offset, true);
  }
  
  Cursor( BNode<K> node, int offset, boolean visit)
  {
    this( null, node, offset, visit);
  }
  
  Cursor( Cursor<K> parent, BNode<K> node, int offset)
  {
    this( parent, node, offset, true);
  }
  
  Cursor( Cursor<K> parent, BNode<K> node, int offset, boolean visit)
  {
    this.parent = parent;
    this.node = node;
    this.offset = offset;
    this.visit = visit;
  }
  
  public Entry<K> get()
  {
    return node.entries.get( offset);
  }

  public boolean hasPrevious()
  {
    return offset >= 0;
  }
  
  public Cursor<K> previous()
  {
    Cursor<K> leaf = previousLeaf();
    if ( leaf != null) return leaf;
    
    if ( offset == 0)
    {
      Cursor<K> parent = previousParent();
      if ( parent != null) return parent;
    }
    
    offset--;
    return this;
  }
  
  private Cursor<K> previousParent()
  {
    Cursor<K> cursor = parent;
    while( cursor != null && cursor.offset == 0)
      cursor = cursor.parent;
    if ( cursor != null) cursor.offset--;
    return cursor;
  }
  
  private Cursor<K> previousLeaf()
  {
    if ( node.children().size() == 0) return null;
    
    BNode<K> child = node.children().get( offset);
    Cursor<K> cursor = new Cursor<K>( this, child, child.count());
    
    while( child.children().size() > 0)
    {
      child = child.children.get( child.count());
      cursor = new Cursor<K>( cursor, child, child.count());
    }

    if ( cursor != null) cursor.offset--;
    return cursor;
  }
  
  public boolean hasNext()
  {
    return offset < node.count();
  }
  
  public Cursor<K> next()
  {
    offset++;
    
    Cursor<K> leaf = nextLeaf();
    if ( leaf != null) return leaf;
    
    if ( offset == node.count())
    {
      Cursor<K> parent = nextParent();
      if ( parent != null) return parent;
    }
    
    return this;
  }
  
  private Cursor<K> nextParent()
  {
    Cursor<K> cursor = parent;
    while( cursor != null && cursor.offset == cursor.node.count())
      cursor = cursor.parent;
    return cursor;
  }
  
  private Cursor<K> nextLeaf()
  {
    if ( node.children().size() == 0) return null;
    
    BNode<K> child = node.children().get( offset);
    Cursor<K> cursor = new Cursor<K>( this, child, 0);
    
    while( child.children().size() > 0)
    {
      child = child.children.get( 0);
      cursor = new Cursor<K>( cursor, child, 0);
    }
    
    return cursor;
  }
  
  public Cursor<K> parent;
  public BNode<K> node;
  public int offset;
  public boolean visit;
}