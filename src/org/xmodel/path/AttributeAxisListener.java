/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * AttributeAxisListener.java
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

import org.xmodel.IModel;
import org.xmodel.IModelObject;
import org.xmodel.IPath;
import org.xmodel.xpath.AttributeHistoryNode;
import org.xmodel.xpath.TextHistoryNode;
import org.xmodel.xpath.expression.IContext;

/**
 * An implementation of IFanoutListener for an IPathElement with the <i>ATTRIBUTE</i> axis.
 */
public class AttributeAxisListener extends FanoutListener
{
  public AttributeAxisListener( IListenerChain chain, int chainIndex)
  {
    super( chain, chainIndex);
  }

  /* (non-Javadoc)
   * @see org.xmodel.path.FanoutListener#installListeners(org.xmodel.IModelObject)
   */
  protected void installListeners( IModelObject object)
  {
    object.addModelListener( this);
  }

  /* (non-Javadoc)
   * @see org.xmodel.path.FanoutListener#uninstallListeners(org.xmodel.IModelObject)
   */
  protected void uninstallListeners( IModelObject object)
  {
    object.removeModelListener( this);
  }

  /* (non-Javadoc)
   * @see org.xmodel.path.IListenerChainLink#cloneOne(org.xmodel.path.IListenerChain)
   */
  public IListenerChainLink cloneOne( IListenerChain chain)
  {
    return new AttributeAxisListener( chain, chainIndex);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.path.ListenerChainLink#notifyChange(
   * org.xmodel.IModelObject, java.lang.String, java.lang.Object, java.lang.Object)
   */
  public void notifyChange( IModelObject object, String attrName, Object newValue, Object oldValue)
  {
    // notify when attribute appears
    if ( oldValue == null)
    {
      IPath path = getListenerChain().getPath();
      int pathIndex = getPathIndex();
      
      IModelObject node = null;
      
      IContext context = getListenerChain().getContext();
      IModel model = context.getModel();
      model.revert();
      try
      {
        if ( attrName.length() == 0)
        {
          // TODO: Is AttributeNode needed here?
          node = object.getAttributeNode( "");
          if ( !fanoutElement.evaluate( context, path, pathIndex, node)) 
            node = null;
        }
        else
        {
          node = object.getAttributeNode( attrName);
          if ( !fanoutElement.evaluate( context, path, pathIndex, node)) 
            node = null;
        }
      }
      finally
      {
        model.restore();
      }
      
      if ( node != null) getNextListener().incrementalInstall( node);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.path.ListenerChainLink#notifyClear(
   * org.xmodel.IModelObject, java.lang.String, java.lang.Object)
   */
  public void notifyClear( IModelObject object, String attrName, Object oldValue)
  {
    // notify when attribute is removed
    if ( oldValue != null)
    {
      IPath path = getListenerChain().getPath();
      int pathIndex = getPathIndex();
      
      IModelObject node = null;
      
      IContext context = getListenerChain().getContext();
      IModel model = context.getModel();
      model.revert();
      try
      {
        if ( attrName.length() == 0)
        {
          node = new TextHistoryNode( oldValue);
          if ( !fanoutElement.evaluate( context, path, pathIndex, node))
            node = null;
        }
        else
        {
          node = new AttributeHistoryNode( attrName, oldValue);
          if ( !fanoutElement.evaluate( context, path, pathIndex, node)) 
            node = null;
        }
      }
      finally
      {
        model.restore();
      }
      
      if ( node != null) getNextListener().incrementalUninstall( node);
    }
  }
}
