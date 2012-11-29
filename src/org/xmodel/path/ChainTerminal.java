/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ChainTerminal.java
 * 
 * Copyright 2009 Robert Arvin Dunnagan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.xmodel.path;

import java.util.Collections;
import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.IPath;
import org.xmodel.IPathListener;
import org.xmodel.log.Log;
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
        log.exception( e);
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
        log.exception( e);
      }
    }
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
        log.exception( e);
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
        log.exception( e);
      }
    }
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
  
  private static Log log = Log.getLog( "org.xmodel.path");
  
  IListenerChain chain;
}
