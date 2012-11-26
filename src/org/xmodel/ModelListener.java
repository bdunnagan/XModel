/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ModelListener.java
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

/**
 * An adapter for the IModelListener interface.
 */
public class ModelListener implements IModelListener
{
  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyParent(org.xmodel.IModelObject, 
   * org.xmodel.IModelObject, org.xmodel.IModelObject)
   */
  public void notifyParent( INode child, INode newParent, INode oldParent)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyAdd(org.xmodel.IModelObject, 
   * org.xmodel.IModelObject, int)
   */
  public void notifyAddChild( INode parent, INode child, int index)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyRemove(org.xmodel.IModelObject, 
   * org.xmodel.IModelObject)
   */
  public void notifyRemoveChild( INode parent, INode child, int index)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyChange(org.xmodel.IModelObject, 
   * java.lang.String, java.lang.String, java.lang.String)
   */
  public void notifyChange( INode object, String attrName, Object newValue, Object oldValue)
  {
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyClear(org.xmodel.IModelObject, 
   * java.lang.String, java.lang.String)
   */
  public void notifyClear( INode object, String attrName, Object oldValue)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyDirty(org.xmodel.IModelObject, boolean)
   */
  public void notifyDirty( INode object, boolean dirty)
  {
    // default behavior is to resync the object since generic listener is interested in everything
    if ( dirty) object.getChildren();
  }
}
