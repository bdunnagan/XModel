/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ValueObserver.java
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
package org.xmodel.listeners;

import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.IPath;
import org.xmodel.IPathListener;
import org.xmodel.ModelListener;
import org.xmodel.xpath.expression.IContext;

/**
 * When this listener is installed on a path, it provides notification for the value of each 
 * node in the node-set returned by the path. It also installs listeners on each node and 
 * provides notification when the value of the node changes.
 */
public abstract class ValueObserver extends ModelListener implements IPathListener
{
  /* (non-Javadoc)
   * @see org.xmodel.ModelListener#notifyChange(org.xmodel.IModelObject, 
   * java.lang.String, java.lang.Object, java.lang.Object)
   */
  @Override
  public void notifyChange( IModelObject object, String attrName, Object newValue, Object oldValue)
  {
    if ( attrName.length() == 0) notifyValue( object, newValue, oldValue);
  }

  /* (non-Javadoc)
   * @see org.xmodel.ModelListener#notifyClear(org.xmodel.IModelObject, 
   * java.lang.String, java.lang.Object)
   */
  @Override
  public void notifyClear( IModelObject object, String attrName, Object oldValue)
  {
    if ( attrName.length() == 0) notifyValue( object, null, oldValue);
  }

  /* (non-Javadoc)
   * @see org.xmodel.ModelListener#notifyDirty(org.xmodel.IModelObject, boolean)
   */
  @Override
  public void notifyDirty( IModelObject object, boolean dirty)
  {
    // resync if necessary
    if ( dirty) object.getValue();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IPathListener#notifyAdd(org.xmodel.xpath.expression.IContext, 
   * org.xmodel.IPath, int, java.util.List)
   */
  public void notifyAdd( IContext context, IPath path, int pathIndex, List<IModelObject> nodes)
  {
    if ( pathIndex != path.length()) return;
    
    for( IModelObject object: nodes)
    {
      object.addModelListener( this);
      Object value = object.getValue();
      if ( value != null) notifyValue( object, value, null);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.IPathListener#notifyRemove(org.xmodel.xpath.expression.IContext, 
   * org.xmodel.IPath, int, java.util.List)
   */
  public void notifyRemove( IContext context, IPath path, int pathIndex, List<IModelObject> nodes)
  {
    if ( pathIndex != path.length()) return;
    
    for( IModelObject object: nodes)
      object.removeModelListener( this);
  }

  /**
   * Called once for each leaf of the path and again whenever the value of a leaf changes.
   * @param object A leaf of the path.
   * @param newValue The new value of the leaf.
   * @param oldValue The old value of the leaf.
   */
  protected abstract void notifyValue( IModelObject object, Object newValue, Object oldValue);
}
