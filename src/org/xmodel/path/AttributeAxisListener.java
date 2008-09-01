/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.path;

import org.xmodel.IModelObject;
import org.xmodel.IPath;
import org.xmodel.xpath.AttributeHistoryNode;
import org.xmodel.xpath.TextHistoryNode;
import org.xmodel.xpath.expression.IContext;

/**
 * An implementation of IFanoutListener for an IPathElement with the <i>ATTRIBUTE</i> axis.
 */
public class AttributeAxisListener extends FanoutListener
{
  public AttributeAxisListener( IListenerChain chain, int chainIndex)
  {
    super( chain, chainIndex);
  }

  /* (non-Javadoc)
   * @see org.xmodel.path.FanoutListener#installListeners(org.xmodel.IModelObject)
   */
  protected void installListeners( IModelObject object)
  {
    object.addModelListener( this);
  }

  /* (non-Javadoc)
   * @see org.xmodel.path.FanoutListener#uninstallListeners(org.xmodel.IModelObject)
   */
  protected void uninstallListeners( IModelObject object)
  {
    object.removeModelListener( this);
  }

  /* (non-Javadoc)
   * @see org.xmodel.path.IListenerChainLink#cloneOne(org.xmodel.path.IListenerChain)
   */
  public IListenerChainLink cloneOne( IListenerChain chain)
  {
    return new AttributeAxisListener( chain, chainIndex);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.path.ListenerChainLink#notifyChange(
   * org.xmodel.IModelObject, java.lang.String, java.lang.Object, java.lang.Object)
   */
  public void notifyChange( IModelObject object, String attrName, Object newValue, Object oldValue)
  {
    // notify when attribute appears
    if ( oldValue == null)
    {
      IPath path = getListenerChain().getPath();
      IContext context = getListenerChain().getContext();
      if ( attrName.length() == 0)
      {
        IModelObject node = object.getAttributeNode( "");
        if ( fanoutElement.evaluate( context, path, node)) 
          getNextListener().incrementalInstall( node);
      }
      else
      {
        IModelObject node = object.getAttributeNode( attrName);
        if ( fanoutElement.evaluate( context, path, node)) 
          getNextListener().incrementalInstall( node);
      }
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.path.ListenerChainLink#notifyClear(
   * org.xmodel.IModelObject, java.lang.String, java.lang.Object)
   */
  public void notifyClear( IModelObject object, String attrName, Object oldValue)
  {
    // notify when attribute is removed
    if ( oldValue != null)
    {
      IPath path = getListenerChain().getPath();
      IContext context = getListenerChain().getContext();
      if ( attrName.length() == 0)
      {
        IModelObject node = new TextHistoryNode( oldValue);
        if ( fanoutElement.evaluate( context, path, node))
          getNextListener().incrementalUninstall( node);
      }
      else
      {
        IModelObject node = new AttributeHistoryNode( attrName, oldValue);
        if ( fanoutElement.evaluate( context, path, node)) 
          getNextListener().incrementalUninstall( node);
      }
    }
  }
}
