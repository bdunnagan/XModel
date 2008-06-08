/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import dunnagan.bob.xmodel.util.Fifo;

/**
 * An iterator which visits all the decendants of a domain object. The tree is visited breadth-first
 * and the root object is the first object visited. References are only visited once to prevent 
 * infinite loops.
 */
public class BreadthFirstIterator implements Iterator<IModelObject>
{
  public BreadthFirstIterator( IModelObject root)
  {
    fifo = new Fifo<IModelObject>();
    fifo.push( root);
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
    Iterator<IModelObject> iter = object.getChildren().iterator();
    while( iter.hasNext()) 
    {
      IModelObject child = (IModelObject)iter.next();
      fifo.push( child);
    }
    return object;
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
