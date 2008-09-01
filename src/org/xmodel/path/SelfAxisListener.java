/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.path;

import org.xmodel.IModelObject;

/**
 * An implementation of IFanoutListener for an IPathElement with the <i>SELF</i> axis.
 */
public class SelfAxisListener extends FanoutListener
{
  public SelfAxisListener( IListenerChain chain, int chainIndex)
  {
    super( chain, chainIndex);
  }

  /* (non-Javadoc)
   * @see org.xmodel.path.FanoutListener#installListeners(org.xmodel.IModelObject)
   */
  protected void installListeners( IModelObject object)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.path.FanoutListener#uninstallListeners(org.xmodel.IModelObject)
   */
  protected void uninstallListeners( IModelObject object)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.path.IListenerChainLink#cloneOne(org.xmodel.path.IListenerChain)
   */
  public IListenerChainLink cloneOne( IListenerChain chain)
  {
    return new SelfAxisListener( chain, chainIndex);
  }
}
