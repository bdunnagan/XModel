/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import dunnagan.bob.xmodel.util.Fifo;

/**
 * An iterator which visits all of the following elements of the starting element (see following axis).
 * References are only visited once to prevent infinite loops.
 */
public class FollowingIterator implements Iterator<IModelObject>
{
  public FollowingIterator( IModelObject root)
  {
    fifo = new Fifo<IModelObject>();
    pushNext( root);
    references = new HashSet<IModelObject>();
  }
  
  /* (non-Javadoc)
   * @see java.util.Iterator#hasNext()
   */
  public boolean hasNext()
  {
    while( !fifo.empty() && !shouldTraverse( fifo.peek())) fifo.pop();
    return !fifo.empty();
  }

  /* (non-Javadoc)
   * @see java.util.Iterator#next()
   */
  public IModelObject next()
  {
    IModelObject object = (IModelObject)fifo.pop();
    pushNext( object);
    return object;
  }
  
  /**
   * Push the next element following the specified object.
   * @param object The object.
   */
  private void pushNext( IModelObject object)
  {
    // push next sibling
    if ( !pushNextSibling( object))
    {
      // else push parent sibling
      IModelObject parent = object.getParent();
      if ( parent != null) pushNextSibling( parent);
    }
  }
  
  /**
   * Push the next sibling of the specified object.
   * @param object The object.
   * @return Returns true if a sibling was found.
   */
  private boolean pushNextSibling( IModelObject object)
  {
    IModelObject parent = object.getParent();
    if ( parent != null)
    {
      List<IModelObject> siblings = parent.getChildren();
      int index = siblings.indexOf( object) + 1;
      if ( index < siblings.size()) 
      {
        fifo.push( siblings.get( index));
        return true;
      }
    }
    return false;
  }

  /* (non-Javadoc)
   * @see java.util.Iterator#remove()
   */
  public void remove()
  {
    throw new UnsupportedOperationException();
  }
  
  /**
   * Returns true if the specified object should be traversed.
   * @param object The object.
   * @return Returns true if the specified object should be traversed.
   */
  protected boolean shouldTraverse( IModelObject object)
  {
    IModelObject referent = object.getReferent();
    if ( referent != object)
    {
      if ( references.contains( object)) return false;
      references.add( object);
    }
    return true;
  }
  
  private Fifo<IModelObject> fifo;
  private Set<IModelObject> references;
}
