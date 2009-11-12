/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * NestedAxisListener.java
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
import org.xmodel.IModelObject;
import org.xmodel.util.Fifo;


/**
 * An implementation of IFanoutListener for an IPathElement with the <i>LIKE_DESCENDANT</i> axis.
 * <br>
 * <b>Note: The following axis listeners exhibit a subtle flaw: AncestorAxisListener,
 * DescendantAxisListener, NestedAxisListener, FollowingAxisListener and PrecedingAxisListener.
 * These listeners install their associated IModelListener instances after fanout (see code below
 * marked with *). A listener chain whose next to last link is one of these axis listeners will not
 * be able to detect that fanout is required if the client adds an element on the axis during
 * notification.</b>
 */
public class NestedAxisListener extends FanoutListener
{
  public NestedAxisListener( IListenerChain chain, int chainIndex)
  {
    super( chain, chainIndex);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.path.FanoutListener#installListeners(org.xmodel.IModelObject)
   */
  protected void installListeners( IModelObject object)
  {
    List<IModelObject> layer = new ArrayList<IModelObject>();
    Fifo<IModelObject> stack = new Fifo<IModelObject>();
    stack.push( object);
    while( !stack.empty())
    {
      IModelObject nested = stack.pop();
      nested.addModelListener( this);
      layer.clear();
      fanoutElement.query( null, nested, layer);
      for( IModelObject child: layer) if ( child != nested) stack.push( child);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.path.FanoutListener#uninstallListeners(org.xmodel.IModelObject)
   */
  protected void uninstallListeners( IModelObject object)
  {
    List<IModelObject> layer = new ArrayList<IModelObject>();
    Fifo<IModelObject> stack = new Fifo<IModelObject>();
    stack.push( object);
    while( !stack.empty())
    {
      IModelObject nested = stack.pop();
      nested.removeModelListener( this);
      layer.clear();
      fanoutElement.query( null, nested, layer);
      for( IModelObject child: layer) if ( child != nested) stack.push( child);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.path.IListenerChainLink#cloneOne(org.xmodel.path.IListenerChain)
   */
  public IListenerChainLink cloneOne( IListenerChain chain)
  {
    return new NestedAxisListener( chain, chainIndex);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.path.ListenerChainLink#notifyAddChild(
   * org.xmodel.IModelObject, org.xmodel.IModelObject, int)
   */
  public void notifyAddChild( IModelObject parent, IModelObject child, int index)
  {
    // install next link (* see above)
    List<IModelObject> layer = new ArrayList<IModelObject>();
    Fifo<IModelObject> stack = new Fifo<IModelObject>();
    stack.push( child);
    while( !stack.empty())
    {
      IModelObject nested = stack.pop();
      if ( fanoutElement.evaluate( null, null, nested))
      {
        getNextListener().incrementalInstall( nested);
        layer.clear();
        fanoutElement.query( null, nested, layer);
        for( IModelObject object: layer) if ( object != nested) stack.push( object);
      }
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
    List<IModelObject> layer = new ArrayList<IModelObject>();
    Fifo<IModelObject> stack = new Fifo<IModelObject>();
    stack.push( child);
    while( !stack.empty())
    {
      IModelObject nested = stack.pop();
      if ( fanoutElement.evaluate( null, null, nested))
      {
        getNextListener().incrementalUninstall( nested);
        layer.clear();
        fanoutElement.query( null, nested, layer);
        for( IModelObject object: layer) if ( object != nested) stack.push( object);
      }
    }
  }
}
