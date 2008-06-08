/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.path;

import java.util.ArrayList;
import java.util.List;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.IPath;
import dunnagan.bob.xmodel.xpath.expression.IContext;

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
   * @see dunnagan.bob.xmodel.path.FanoutListener#installListeners(dunnagan.bob.xmodel.IModelObject)
   */
  protected void installListeners( IModelObject object)
  {
    IModelObject ancestor = object;
    while( ancestor != null)
    {
      ancestor.addModelListener( this);
      ancestor = ancestor.getParent();
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.path.FanoutListener#uninstallListeners(dunnagan.bob.xmodel.IModelObject)
   */
  protected void uninstallListeners( IModelObject object)
  {
    IModelObject ancestor = object;
    while( ancestor != null)
    {
      ancestor.removeModelListener( this);
      ancestor = ancestor.getParent();
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.path.IListenerChainLink#cloneOne(dunnagan.bob.xmodel.path.IListenerChain)
   */
  public IListenerChainLink cloneOne( IListenerChain chain)
  {
    return new AncestorAxisListener( chain, chainIndex);
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.path.ListenerChainLink#notifyParent(dunnagan.bob.xmodel.IModelObject, 
   * dunnagan.bob.xmodel.IModelObject, dunnagan.bob.xmodel.IModelObject)
   */
  public void notifyParent( IModelObject child, IModelObject newParent, IModelObject oldParent)
  {
    IPath path = getListenerChain().getPath();
    IContext context = getListenerChain().getContext();
    if ( oldParent != null)
    {
      // uninstall my listeners
      uninstallListeners( oldParent);

      // uninstall next link
      List<IModelObject> list = new ArrayList<IModelObject>( 3);
      IModelObject ancestor = oldParent;
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
      List<IModelObject> list = new ArrayList<IModelObject>( 3);
      IModelObject ancestor = newParent;
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
