/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.path;

import dunnagan.bob.xmodel.IModelObject;

/**
 * An implementation of IFanoutListener for an IPathElement with the <i>PARENT</i> axis.
 */
public class ParentAxisListener extends FanoutListener
{
  public ParentAxisListener( IListenerChain chain, int chainIndex)
  {
    super( chain, chainIndex);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.path.FanoutListener#installListeners(dunnagan.bob.xmodel.IModelObject)
   */
  protected void installListeners( IModelObject object)
  {
    object.addModelListener( this);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.path.FanoutListener#uninstallListeners(dunnagan.bob.xmodel.IModelObject)
   */
  protected void uninstallListeners( IModelObject object)
  {
    object.removeModelListener( this);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.path.IListenerChainLink#cloneOne(dunnagan.bob.xmodel.path.IListenerChain)
   */
  public IListenerChainLink cloneOne( IListenerChain chain)
  {
    return new ParentAxisListener( chain, chainIndex);
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.path.ListenerChainLink#notifyParent(dunnagan.bob.xmodel.IModelObject, 
   * dunnagan.bob.xmodel.IModelObject, dunnagan.bob.xmodel.IModelObject)
   */
  public void notifyParent( IModelObject child, IModelObject newParent, IModelObject oldParent)
  {
    if ( oldParent != null) getNextListener().incrementalUninstall( oldParent);
    if ( newParent != null) getNextListener().incrementalInstall( newParent);
  }
}
