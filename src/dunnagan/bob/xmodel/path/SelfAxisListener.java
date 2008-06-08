/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.path;

import dunnagan.bob.xmodel.IModelObject;

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
   * @see dunnagan.bob.xmodel.path.FanoutListener#installListeners(dunnagan.bob.xmodel.IModelObject)
   */
  protected void installListeners( IModelObject object)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.path.FanoutListener#uninstallListeners(dunnagan.bob.xmodel.IModelObject)
   */
  protected void uninstallListeners( IModelObject object)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.path.IListenerChainLink#cloneOne(dunnagan.bob.xmodel.path.IListenerChain)
   */
  public IListenerChainLink cloneOne( IListenerChain chain)
  {
    return new SelfAxisListener( chain, chainIndex);
  }
}
