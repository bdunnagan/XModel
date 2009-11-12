/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * LeafListener.java
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
import org.xmodel.*;
import org.xmodel.xpath.expression.IContext;


/**
 * A convenience class for notification for the addition and removal of leaves of the specified
 * model path. The notification happens regardless of how the object is added to or removed from the
 * model. The path may contain elements with any axis including ANCESTOR and DECENDENT.
 */
public class LeafListener extends ModelListener implements IPathListener
{
  /**
   * Create a LeafListener.
   */
  public LeafListener()
  {
    listener = this;
  }

  /**
   * Create a LeafListener which installs the given IModelListener on the leaves. The listener
   * will automatically be uninstalled when the leaf is removed from the path.
   * @param listener The listener to be installed.
   */
  public LeafListener( IModelListener listener)
  {
    this.listener = listener;
  }
  
  /**
   * Called when a domain object on the specified model path is added.
   * @param context The root of the path query.
   * @param path The model path of the domain object.
   * @param object The domain object which was added.
   */
  public void notifyAdd( IContext context, IPath path, List<IModelObject> nodes)
  {
  }

  /**
   * Called when a domain object on the specified model path is removed.
   * @param context The root of the path query.
   * @param path The model path of the domain object.
   * @param object The domain object which was removed.
   */
  public void notifyRemove( IContext context, IPath path, List<IModelObject> nodes)
  {
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IPathListener#notifyAdd(org.xmodel.xpath.expression.IContext, 
   * org.xmodel.IPath, int, java.util.List)
   */
  public void notifyAdd( IContext context, IPath path, int pathIndex, List<IModelObject> nodes)
  {
    if ( pathIndex == path.length()) 
    {
      for( IModelObject node: nodes) node.addModelListener( listener);
      notifyAdd( context, path, nodes);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.IPathListener#notifyRemove(org.xmodel.xpath.expression.IContext, 
   * org.xmodel.IPath, int, java.util.List)
   */
  public void notifyRemove( IContext context, IPath path, int pathIndex, List<IModelObject> nodes)
  {
    if ( pathIndex == path.length()) 
    {
      notifyRemove( context, path, nodes);
      for( IModelObject node: nodes) node.removeModelListener( listener);
    }
  }
  
  IModelListener listener;
}
