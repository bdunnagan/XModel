/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.path;

import java.util.List;

import dunnagan.bob.xmodel.IModelListener;
import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.IPath;
import dunnagan.bob.xmodel.IPathListener;
import dunnagan.bob.xmodel.xpath.expression.IContext;

/**
 * A base implementation of IListenerChainLink.
 */
public abstract class ListenerChainLink implements IListenerChainLink, IModelListener
{
  protected ListenerChainLink( IListenerChain chain, int chainIndex)
  {
    this.chain = chain;
    this.chainIndex = chainIndex;
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.path.IListenerChainLink#bind(dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  public void bind( IContext context)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.path.IListenerChainLink#unbind(dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  public void unbind( IContext context)
  {
  }

  /**
   * Notify the IPathListener that nodes were added.
   * @param list The nodes that were added.
   */
  protected void notifyAdd( List<IModelObject> list)
  {
    if ( list.size() == 0) return;
    IPathListener listener = chain.getPathListener();
    if ( listener != null)
    {
      try
      {
        IPath path = chain.getPath();
        listener.notifyAdd( chain.getContext(), path, chainIndex, list);
      }
      catch( Exception e)
      {
        e.printStackTrace( System.err);
      }
    }
  }

  /**
   * Notify the IPathListener that a nodes were removed.
   * @param list The nodes that were removed.
   */
  protected void notifyRemove( List<IModelObject> list)
  {
    if ( list.size() == 0) return;
    IPathListener listener = chain.getPathListener();
    if ( listener != null)
    {
      try
      {
        IPath path = chain.getPath();
        listener.notifyRemove( chain.getContext(), path, chainIndex, list);
      }
      catch( Exception e)
      {
        e.printStackTrace( System.err);
      }
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.path.IListenerChainLink#getListenerChain()
   */
  public IListenerChain getListenerChain()
  {
    return chain;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.path.IListenerChainLink#getPathIndex()
   */
  public int getPathIndex()
  {
    return chainIndex;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    return chain.toString();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelListener#notifyParent(dunnagan.bob.xmodel.IModelObject, 
   * dunnagan.bob.xmodel.IModelObject, dunnagan.bob.xmodel.IModelObject)
   */
  public void notifyParent( IModelObject child, IModelObject newParent, IModelObject oldParent)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelListener#notifyAddChild(dunnagan.bob.xmodel.IModelObject, 
   * dunnagan.bob.xmodel.IModelObject, int)
   */
  public void notifyAddChild( IModelObject parent, IModelObject child, int index)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelListener#notifyRemoveChild(dunnagan.bob.xmodel.IModelObject, 
   * dunnagan.bob.xmodel.IModelObject, int)
   */
  public void notifyRemoveChild( IModelObject parent, IModelObject child, int index)
  {
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelListener#notifyChange(dunnagan.bob.xmodel.IModelObject, java.lang.String, 
   * java.lang.Object, java.lang.Object)
   */
  public void notifyChange( IModelObject object, String attrName, Object newValue, Object oldValue)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelListener#notifyClear(dunnagan.bob.xmodel.IModelObject, java.lang.String, 
   * java.lang.Object)
   */
  public void notifyClear( IModelObject object, String attrName, Object oldValue)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelListener#notifyDirty(dunnagan.bob.xmodel.IModelObject, boolean)
   */
  public void notifyDirty( IModelObject object, boolean dirty)
  {
  }

  IListenerChain chain;
  int chainIndex;
}
