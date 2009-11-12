/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * NonSyncingIterator.java
 * 
 * Copyright 2009 Robert Arvin Dunnagan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.xmodel.external;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.xmodel.IModelObject;
import org.xmodel.util.Fifo;


/**
 * A BreadthFirstIterator which does not sync IExternalReferences.
 */
public class NonSyncingIterator implements Iterator<IModelObject>
{
  public NonSyncingIterator( IModelObject root)
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
    if ( !object.isDirty())
    {
      Iterator<IModelObject> iter = object.getChildren().iterator();
      while( iter.hasNext()) 
      {
        IModelObject child = (IModelObject)iter.next();
        fifo.push( child);
      }
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
  
  Fifo<IModelObject> fifo;
  Set<IModelObject> references;
}
