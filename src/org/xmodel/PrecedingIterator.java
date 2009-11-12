/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * PrecedingIterator.java
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
package org.xmodel;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.xmodel.util.Fifo;


/**
 * An iterator which visits all of the preceding elements of the starting element (see preceding axis).
 * References are only visited once to prevent infinite loops.
 */
public class PrecedingIterator implements Iterator<IModelObject>
{
  public PrecedingIterator( IModelObject root)
  {
    fifo = new Fifo<IModelObject>();
    pushPrevious( root);
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
    pushPrevious( object);
    return object;
  }
  
  /**
   * Push the previous element following the specified object.
   * @param object The object.
   */
  private void pushPrevious( IModelObject object)
  {
    // push next sibling
    if ( !pushPreviousSibling( object))
    {
      // else push parent sibling
      IModelObject parent = object.getParent();
      if ( parent != null) pushPreviousSibling( parent);
    }
  }
  
  /**
   * Push the previous sibling of the specified object.
   * @param object The object.
   * @return Returns true if a sibling was found.
   */
  private boolean pushPreviousSibling( IModelObject object)
  {
    IModelObject parent = object.getParent();
    if ( parent != null)
    {
      List<IModelObject> siblings = parent.getChildren();
      int index = siblings.indexOf( object) - 1;
      if ( index >= 0) 
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
