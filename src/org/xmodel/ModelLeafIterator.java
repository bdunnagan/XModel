/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ModelLeafIterator.java
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
