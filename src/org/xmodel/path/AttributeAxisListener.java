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

import org.xmodel.INode;
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
  protected void installListeners( INode object)
  {
    object.addModelListener( this);
  }

  /* (non-Javadoc)
   * @see org.xmodel.path.FanoutListener#uninstallListeners(org.xmodel.IModelObject)
   */
  protected void uninstallListeners( INode object)
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
  public void notifyChange( INode object, String attrName, Object newValue, Object oldValue)
  {
    // notify when attribute appears
    if ( oldValue == null)
    {
      IPath path = getListenerChain().getPath();
      IContext context = getListenerChain().getContext();
      if ( attrName.length() == 0)
      {
        INode node = object.getAttributeNode( "");
        if ( fanoutElement.evaluate( context, path, node)) 
          getNextListener().incrementalInstall( node);
      }
      else
      {
        INode node = object.getAttributeNode( attrName);
        if ( fanoutElement.evaluate( context, path, node)) 
          getNextListener().incrementalInstall( node);
      }
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.path.ListenerChainLink#notifyClear(
   * org.xmodel.IModelObject, java.lang.String, java.lang.Object)
   */
  public void notifyClear( INode object, String attrName, Object oldValue)
  {
    // notify when attribute is removed
    if ( oldValue != null)
    {
      IPath path = getListenerChain().getPath();
      IContext context = getListenerChain().getContext();
      if ( attrName.length() == 0)
      {
        INode node = new TextHistoryNode( oldValue);
        if ( fanoutElement.evaluate( context, path, node))
          getNextListener().incrementalUninstall( node);
      }
      else
      {
        INode node = new AttributeHistoryNode( attrName, oldValue);
        if ( fanoutElement.evaluate( context, path, node)) 
          getNextListener().incrementalUninstall( node);
      }
    }
  }
}
