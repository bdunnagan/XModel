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
 * An implementation of IFanoutListener for an IPathElement with the <i>ANCESTOR</i> axis.
 */
public class RootAxisListener extends FanoutListener
{
  public RootAxisListener( IListenerChain chain, int chainIndex)
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
      object.addModelListener( this);
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
      object.removeModelListener( this);
      ancestor = ancestor.getParent();
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.path.IListenerChainLink#cloneOne(dunnagan.bob.xmodel.path.IListenerChain)
   */
  public IListenerChainLink cloneOne( IListenerChain chain)
  {
    return new RootAxisListener( chain, chainIndex);
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
