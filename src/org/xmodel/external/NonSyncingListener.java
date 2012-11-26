/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * NonSyncingListener.java
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

import org.xmodel.INode;
import org.xmodel.ModelListener;

/**
 * An IModelListener which fans-out into the subtree without syncing external references.
 * When external references are synced in some other way, however, the listener will fan-out.
 */
public class NonSyncingListener extends ModelListener
{
  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyParent(org.xmodel.IModelObject, 
   * org.xmodel.IModelObject, org.xmodel.IModelObject)
   */
  public void notifyParent( INode child, INode newParent, INode oldParent)
  {
    if ( newParent == null) uninstall( child);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyAddChild(org.xmodel.IModelObject, 
   * org.xmodel.IModelObject, int)
   */
  public void notifyAddChild( INode parent, INode child, int index)
  {
    install( child);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyRemoveChild(org.xmodel.IModelObject, 
   * org.xmodel.IModelObject, int)
   */
  public void notifyRemoveChild( INode parent, INode child, int index)
  {
    uninstall( child);
  }

  /**
   * Install this listener in the specified element and descendants.
   * @param element The element.
   */
  public void install( INode element)
  {
    NonSyncingIterator iter = new NonSyncingIterator( element);
    while ( iter.hasNext())
    {
      INode object = (INode)iter.next();
      object.addModelListener( this);
    }
  }

  /**
   * Remove this listener from the specified element and descendants.
   * @param element The element.
   */
  public void uninstall( INode element)
  {
    NonSyncingIterator iter = new NonSyncingIterator( element);
    while ( iter.hasNext())
    {
      INode object = (INode)iter.next();
      object.removeModelListener( this);
    }
  }
}
