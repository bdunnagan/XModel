/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * AncestorAxisListener.java
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

import java.util.ArrayList;
import java.util.List;
import org.xmodel.INode;
import org.xmodel.IPath;
import org.xmodel.xpath.expression.IContext;


/**
 * An implementation of IFanoutListener for an IPathElement with the <i>ANCESTOR</i> axis.
 * <br>
 * <b>Note: The following axis listeners exhibit a subtle flaw: AncestorAxisListener,
 * DescendantAxisListener, NestedAxisListener, FollowingAxisListener and PrecedingAxisListener.
 * These listeners install their associated IModelListener instances after fanout (see code below
 * marked with *). A listener chain whose next to last link is one of these axis listeners will not
 * be able to detect that fanout is required if the client adds an element on the axis during
 * notification.</b>
 */
public class AncestorAxisListener extends FanoutListener
{
  public AncestorAxisListener( IListenerChain chain, int chainIndex)
  {
    super( chain, chainIndex);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.path.FanoutListener#installListeners(org.xmodel.IModelObject)
   */
  protected void installListeners( INode object)
  {
    INode ancestor = object;
    while( ancestor != null)
    {
      ancestor.addModelListener( this);
      ancestor = ancestor.getParent();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.path.FanoutListener#uninstallListeners(org.xmodel.IModelObject)
   */
  protected void uninstallListeners( INode object)
  {
    INode ancestor = object;
    while( ancestor != null)
    {
      ancestor.removeModelListener( this);
      ancestor = ancestor.getParent();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.path.IListenerChainLink#cloneOne(org.xmodel.path.IListenerChain)
   */
  public IListenerChainLink cloneOne( IListenerChain chain)
  {
    return new AncestorAxisListener( chain, chainIndex);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.path.ListenerChainLink#notifyParent(org.xmodel.IModelObject, 
   * org.xmodel.IModelObject, org.xmodel.IModelObject)
   */
  public void notifyParent( INode child, INode newParent, INode oldParent)
  {
    IPath path = getListenerChain().getPath();
    IContext context = getListenerChain().getContext();
    if ( oldParent != null)
    {
      // uninstall my listeners
      uninstallListeners( oldParent);

      // uninstall next link
      List<INode> list = new ArrayList<INode>( 3);
      INode ancestor = oldParent;
      while( ancestor != null)
      {
        if ( fanoutElement.evaluate( context, path, ancestor)) list.add( ancestor);
        ancestor = ancestor.getParent();
      }
      getNextListener().incrementalUninstall( list);
    }
    
    if ( newParent != null)
    {
      // install next link (* see above)
      List<INode> list = new ArrayList<INode>( 3);
      INode ancestor = newParent;
      while( ancestor != null)
      {
        if ( fanoutElement.evaluate( context, path, ancestor)) list.add( ancestor);
        ancestor = ancestor.getParent();
      }
      getNextListener().incrementalInstall( list);

      // install my listeners (* see above)
      installListeners( newParent);
    }
  }
}
