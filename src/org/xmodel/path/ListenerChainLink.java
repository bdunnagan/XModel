/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ListenerChainLink.java
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
package org.xmodel.path;

import java.util.List;
import org.xmodel.IModelListener;
import org.xmodel.IModelObject;
import org.xmodel.IPath;
import org.xmodel.IPathListener;
import org.xmodel.log.Log;
import org.xmodel.xpath.expression.IContext;


/**
 * A base implementation of IListenerChainLink.
 */
public abstract class ListenerChainLink implements IListenerChainLink, IModelListener
{
  protected ListenerChainLink( IListenerChain chain, int chainIndex)
  {
    this.chain = chain;
    this.chainIndex = chainIndex;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.path.IListenerChainLink#bind(org.xmodel.xpath.expression.IContext)
   */
  public void bind( IContext context)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.path.IListenerChainLink#unbind(org.xmodel.xpath.expression.IContext)
   */
  public void unbind( IContext context)
  {
  }

  /**
   * Notify the IPathListener that nodes were added.
   * @param list The nodes that were added.
   */
  protected void notifyAdd( List<IModelObject> list)
  {
    if ( list.size() == 0) return;
    IPathListener listener = chain.getPathListener();
    if ( listener != null)
    {
      try
      {
        IPath path = chain.getPath();
        listener.notifyAdd( chain.getContext(), path, chainIndex, list);
      }
      catch( Exception e)
      {
        log.exception( e);
      }
    }
  }

  /**
   * Notify the IPathListener that a nodes were removed.
   * @param list The nodes that were removed.
   */
  protected void notifyRemove( List<IModelObject> list)
  {
    if ( list.size() == 0) return;
    IPathListener listener = chain.getPathListener();
    if ( listener != null)
    {
      try
      {
        IPath path = chain.getPath();
        listener.notifyRemove( chain.getContext(), path, chainIndex, list);
      }
      catch( Exception e)
      {
        log.exception( e);
      }
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.path.IListenerChainLink#getListenerChain()
   */
  public IListenerChain getListenerChain()
  {
    return chain;
  }

  /* (non-Javadoc)
   * @see org.xmodel.path.IListenerChainLink#getPathIndex()
   */
  public int getPathIndex()
  {
    return chainIndex;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    return chain.toString();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyParent(org.xmodel.IModelObject, 
   * org.xmodel.IModelObject, org.xmodel.IModelObject)
   */
  public void notifyParent( IModelObject child, IModelObject newParent, IModelObject oldParent)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyAddChild(org.xmodel.IModelObject, 
   * org.xmodel.IModelObject, int)
   */
  public void notifyAddChild( IModelObject parent, IModelObject child, int index)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyRemoveChild(org.xmodel.IModelObject, 
   * org.xmodel.IModelObject, int)
   */
  public void notifyRemoveChild( IModelObject parent, IModelObject child, int index)
  {
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyChange(org.xmodel.IModelObject, java.lang.String, 
   * java.lang.Object, java.lang.Object)
   */
  public void notifyChange( IModelObject object, String attrName, Object newValue, Object oldValue)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyClear(org.xmodel.IModelObject, java.lang.String, 
   * java.lang.Object)
   */
  public void notifyClear( IModelObject object, String attrName, Object oldValue)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyDirty(org.xmodel.IModelObject, boolean)
   */
  public void notifyDirty( IModelObject object, boolean dirty)
  {
  }

  private static Log log = Log.getLog( "org.xmodel.path");
  
  IListenerChain chain;
  int chainIndex;
}
