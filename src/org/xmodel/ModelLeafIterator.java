/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel;

import java.util.Iterator;
import org.xmodel.util.Fifo;

/**
 * An iterator which visits all the leaves of a sub-tree.
 */
public class ModelLeafIterator implements Iterator<IModelObject>
{
  public ModelLeafIterator( IModelObject root)
  {
    fifo = new Fifo<IModelObject>();
    fifo.push( root);
  }
  
  /* (non-Javadoc)
   * @see java.util.Iterator#hasNext()
   */
  public boolean hasNext()
  {
    return !fifo.empty();
  }

  /* (non-Javadoc)
   * @see java.util.Iterator#next()
   */
  public IModelObject next()
  {
    while( true)
    {
      IModelObject object = (IModelObject)fifo.pop();
      Iterator<IModelObject> iter = object.getChildren().iterator();
      while( iter.hasNext()) fifo.push( iter.next());
      if ( object.getNumberOfChildren() == 0) return object;
    }
  }

  /* (non-Javadoc)
   * @see java.util.Iterator#remove()
   */
  public void remove()
  {
    throw new UnsupportedOperationException();
  }
  
  Fifo<IModelObject> fifo;
}
