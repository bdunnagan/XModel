/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.path;

import java.util.Collections;
import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.IPath;
import org.xmodel.IPathListener;
import org.xmodel.xpath.expression.IContext;


/**
 * An implementation of IListenerChainLink which terminates the listener chain and provides notification
 * to the client IPathListener for the leaves of the path.
 */
public class ChainTerminal implements IListenerChainLink
{
  public ChainTerminal( IListenerChain chain)
  {
    this.chain = chain;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.path.IListenerChainLink#bind(org.xmodel.xpath.expression.IContext)
   */
  public void bind( IContext context)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.path.IListenerChainLink#unbind(org.xmodel.xpath.expression.IContext)
   */
  public void unbind( IContext context)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.path.IListenerChainLink#install(java.util.List)
   */
  public void install( List<IModelObject> list)
  {
    // debug
    getListenerChain().debugInstall( list, getPathIndex());

    IPathListener listener = chain.getPathListener();
    if ( listener != null)
    {
      try
      {
        IPath path = chain.getPath();
        listener.notifyAdd( chain.getContext(), path, path.length(), list);
      }
      catch( Exception e)
      {
        e.printStackTrace( System.err);
      }
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.path.IListenerChainLink#uninstall(java.util.List)
   */
  public void uninstall( List<IModelObject> list)
  {
    IPathListener listener = chain.getPathListener();
    if ( listener != null)
    {
      try
      {
        IPath path = chain.getPath();
        listener.notifyRemove( chain.getContext(), path, path.length(), list);
      }
      catch( Exception e)
      {
        e.printStackTrace( System.err);
      }
    }
    
    // debug
    getListenerChain().debugUninstall( list, getPathIndex());
  }

  /* (non-Javadoc)
   * @see org.xmodel.path.IListenerChainLink#incrementalInstall(org.xmodel.IModelObject)
   */
  public void incrementalInstall( IModelObject object)
  {
    incrementalInstall( Collections.singletonList( object));
  }

  /* (non-Javadoc)
   * @see org.xmodel.path.IListenerChainLink#incrementalUninstall(org.xmodel.IModelObject)
   */
  public void incrementalUninstall( IModelObject object)
  {
    incrementalUninstall( Collections.singletonList( object));
  }

  /* (non-Javadoc)
   * @see org.xmodel.path.IListenerChainLink#incrementalInstall(java.util.List)
   */
  public void incrementalInstall( List<IModelObject> list)
  {
    // debug
    getListenerChain().debugIncrementalInstall( list, getPathIndex());
    
    IPathListener listener = chain.getPathListener();
    if ( listener != null)
    {
      try
      {
        IPath path = chain.getPath();
        listener.notifyAdd( chain.getContext(), path, path.length(), list);
      }
      catch( Exception e)
      {
        e.printStackTrace( System.err);
      }
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.path.IListenerChainLink#incrementalUninstall(java.util.List)
   */
  public void incrementalUninstall( List<IModelObject> list)
  {
    IPathListener listener = chain.getPathListener();
    if ( listener != null)
    {
      try
      {
        IPath path = chain.getPath();
        listener.notifyRemove( chain.getContext(), path, path.length(), list);
      }
      catch( Exception e)
      {
        e.printStackTrace( System.err);
      }
    }

    // debug
    getListenerChain().debugIncrementalUninstall( list, getPathIndex());
  }

  /* (non-Javadoc)
   * @see org.xmodel.path.IListenerChainLink#getListenerChain()
   */
  public IListenerChain getListenerChain()
  {
    return chain;
  }

  /* (non-Javadoc)
   * @see org.xmodel.path.IListenerChainLink#getPathIndex()
   */
  public int getPathIndex()
  {
    return chain.getPath().length();
  }
  

  /* (non-Javadoc)
   * @see org.xmodel.path.IListenerChainLink#cloneOne(org.xmodel.path.IListenerChain)
   */
  public IListenerChainLink cloneOne( IListenerChain chain)
  {
    return new ChainTerminal( chain);
  }
  
  IListenerChain chain;
}
