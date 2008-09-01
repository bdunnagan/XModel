/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.path;

import org.xmodel.IModelObject;
import org.xmodel.IPath;
import org.xmodel.xpath.expression.IContext;

/**
 * An implementation of IFanoutListener for an IPathElement with the <i>ANCESTOR</i> axis.
 */
public class RootAxisListener extends FanoutListener
{
  public RootAxisListener( IListenerChain chain, int chainIndex)
  {
    super( chain, chainIndex);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.path.FanoutListener#installListeners(org.xmodel.IModelObject)
   */
  protected void installListeners( IModelObject object)
  {
    IModelObject ancestor = object;
    while( ancestor != null)
    {
      object.addModelListener( this);
      ancestor = ancestor.getParent();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.path.FanoutListener#uninstallListeners(org.xmodel.IModelObject)
   */
  protected void uninstallListeners( IModelObject object)
  {
    IModelObject ancestor = object;
    while( ancestor != null)
    {
      object.removeModelListener( this);
      ancestor = ancestor.getParent();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.path.IListenerChainLink#cloneOne(org.xmodel.path.IListenerChain)
   */
  public IListenerChainLink cloneOne( IListenerChain chain)
  {
    return new RootAxisListener( chain, chainIndex);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.path.ListenerChainLink#notifyParent(org.xmodel.IModelObject, 
   * org.xmodel.IModelObject, org.xmodel.IModelObject)
   */
  public void notifyParent( IModelObject child, IModelObject newParent, IModelObject oldParent)
  {
    IPath path = getListenerChain().getPath();
    IContext context = getListenerChain().getContext();
    if ( oldParent != null)
    {
      uninstallListeners( oldParent);

      // uninstall next link
      IModelObject root = oldParent.getRoot();
      if ( fanoutElement.evaluate( context, path, root))
        getNextListener().incrementalUninstall( root);
    }
    else
    {
      if ( fanoutElement.evaluate( context, path, child))
        getNextListener().incrementalUninstall( child);
    }
    
    if ( newParent != null)
    {
      installListeners( newParent);

      // install next link
      IModelObject root = child.getRoot();
      if ( fanoutElement.evaluate( context, path, root))
        getNextListener().incrementalInstall( root);
    }
    else
    {
      if ( fanoutElement.evaluate( context, path, child))
        getNextListener().incrementalInstall( child);
    }
  }
}
