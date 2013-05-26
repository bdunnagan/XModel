/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * FollowingAxisListener.java
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

import org.xmodel.BreadthFirstIterator;
import org.xmodel.FollowingIterator;
import org.xmodel.IModelObject;


/**
 * An implementation of IFanoutListener for an IPathElement with the <i>FOLLOWING</i> axis. <br>
 * 
 * <b>Note: The following axis listeners exhibit a subtle flaw: AncestorAxisListener,
 * DescendantAxisListener, NestedAxisListener, FollowingAxisListener and PrecedingAxisListener.
 * These listeners install their associated IModelListener instances after fanout (see code below
 * marked with *). A listener chain whose next to last link is one of these axis listeners will not
 * be able to detect that fanout is required if the client adds an element on the axis during
 * notification.</b>
 */
public class FollowingAxisListener extends FanoutListener
{
  public FollowingAxisListener( IListenerChain chain, int chainIndex)
  {
    super( chain, chainIndex);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.path.FanoutListener#installListeners(org.xmodel.IModelObject)
   */
  protected void installListeners( IModelObject object)
  {
    // TODO: need to add support for sibling notification in IModelListener & IModelObject
    FollowingIterator iter = new FollowingIterator( object);
    while( iter.hasNext())
    {
      IModelObject following = (IModelObject)iter.next();
      following.addModelListener( this);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.path.FanoutListener#uninstallListeners(org.xmodel.IModelObject)
   */
  protected void uninstallListeners( IModelObject object)
  {
    FollowingIterator iter = new FollowingIterator( object);
    while( iter.hasNext())
    {
      IModelObject following = (IModelObject)iter.next();
      following.removeModelListener( this);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.path.IListenerChainLink#cloneOne(org.xmodel.path.IListenerChain)
   */
  public IListenerChainLink cloneOne( IListenerChain chain)
  {
    return new FollowingAxisListener( chain, chainIndex);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.path.ListenerChainLink#notifyAddChild(
   * org.xmodel.IModelObject, org.xmodel.IModelObject, int)
   */
  public void notifyAddChild( IModelObject parent, IModelObject child, int index)
  {
    // install next link (* see above)
    FollowingIterator iter = new FollowingIterator( child);
    while( iter.hasNext())
    {
      IModelObject following = (IModelObject)iter.next();
      if ( fanoutElement.evaluate( null, null, following)) 
        getNextListener().incrementalInstall( following);
    }
    
    // install my listeners (* see above)
    installListeners( child);
  }

  /* (non-Javadoc)
   * @see org.xmodel.path.ListenerChainLink#notifyRemoveChild(
   * org.xmodel.IModelObject, org.xmodel.IModelObject, int)
   */
  public void notifyRemoveChild( IModelObject parent, IModelObject child, int index)
  {
    // uninstall my listeners
    uninstallListeners( child);

    // uninstall next link
    BreadthFirstIterator iter = new BreadthFirstIterator( child);
    while( iter.hasNext())
    {
      IModelObject descendant = (IModelObject)iter.next();
      if ( fanoutElement.evaluate( null, null, descendant)) 
        getNextListener().incrementalUninstall( descendant);
    }
  }
}
