/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.path;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.IPath;
import dunnagan.bob.xmodel.xpath.expression.IContext;

/**
 * An implementation of IFanoutListener for an IPathElement with the <i>CHILD</i> axis.
 */
public class ChildAxisListener extends FanoutListener
{
  public ChildAxisListener( IListenerChain chain, int chainIndex)
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
    return new ChildAxisListener( chain, chainIndex);
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.ModelListener#notifyAddChild(dunnagan.bob.xmodel.IModelObject, 
   * dunnagan.bob.xmodel.IModelObject, int)
   */
  public void notifyAddChild( IModelObject parent, IModelObject child, int index)
  {
    IPath path = getListenerChain().getPath();
    IContext context = getListenerChain().getContext();
    if ( fanoutElement.evaluate( context, path, child))
      getNextListener().incrementalInstall( child);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.ModelListener#notifyRemoveChild(dunnagan.bob.xmodel.IModelObject, 
   * dunnagan.bob.xmodel.IModelObject, int)
   */
  public void notifyRemoveChild( IModelObject parent, IModelObject child, int index)
  {
    IPath path = getListenerChain().getPath();
    IContext context = getListenerChain().getContext();
    if ( fanoutElement.evaluate( context, path, child))
      getNextListener().incrementalUninstall( child);
  }
}
