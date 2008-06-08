/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.path;

import java.util.Collections;
import java.util.List;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.IPathElement;
import dunnagan.bob.xmodel.xpath.PathElement;
import dunnagan.bob.xmodel.xpath.expression.IContext;

/**
 * A base implementation of IFanoutListener which does the fanout during install/uninstall.
 */
public abstract class FanoutListener extends ListenerChainLink
{
  protected FanoutListener( IListenerChain chain, int chainIndex)
  {
    super( chain, chainIndex);
    IPathElement pathElement = chain.getPath().getPathElement( chainIndex);
    fanoutElement = new PathElement( pathElement.axis(), pathElement.type());
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.path.IListenerChainLink#install(java.util.List)
   */
  public void install( List<IModelObject> list)
  {
    // debug
    getListenerChain().debugInstall( list, getPathIndex());

    // install listeners and do notification
    notifyAdd( list);
    for ( IModelObject object: list) installListeners( object);
    
    // install next link
    IContext context = getListenerChain().getContext();
    List<IModelObject> result = fanoutElement.query( context, list, null);
    IListenerChainLink nextListener = getNextListener();
    if ( result.size() > 0) nextListener.install( result);    
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.path.IListenerChainLink#uninstall(java.util.List)
   */
  public void uninstall( List<IModelObject> list)
  {
    // uninstall listeners
    for ( IModelObject object: list) uninstallListeners( object);
    
    // uninstall next link
    IContext context = getListenerChain().getContext();
    List<IModelObject> result = fanoutElement.query( context, list, null);
    IListenerChainLink nextListener = getNextListener();
    if ( result.size() > 0) nextListener.uninstall( result);    

    // do notification
    notifyRemove( list);
    
    // debug
    getListenerChain().debugUninstall( list, getPathIndex());
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.path.IListenerChainLink#incrementalInstall(dunnagan.bob.xmodel.IModelObject)
   */
  public void incrementalInstall( IModelObject object)
  {
    incrementalInstall( Collections.singletonList( object));
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.path.IListenerChainLink#incrementalUninstall(dunnagan.bob.xmodel.IModelObject)
   */
  public void incrementalUninstall( IModelObject object)
  {
    incrementalUninstall( Collections.singletonList( object));
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.path.IListenerChainLink#incrementalInstall(java.util.List)
   */
  public void incrementalInstall( List<IModelObject> list)
  {
    // debug
    getListenerChain().debugIncrementalInstall( list, getPathIndex());
    
    // do notification
    notifyAdd( list);
    
    // install next link
    IContext context = getListenerChain().getContext();
    IListenerChainLink nextListener = getNextListener();
    List<IModelObject> fanoutNodes = fanoutElement.query( context, list, null);
    if ( fanoutNodes.size() > 0) nextListener.incrementalInstall( fanoutNodes);

    // install listeners after fanout to avoid specious notifications such as when an
    // IExternalReference is synced because of the fanout query above.
    for( IModelObject object: list) installListeners( object);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.path.IListenerChainLink#incrementalUninstall(java.util.List)
   */
  public void incrementalUninstall( List<IModelObject> list)
  {
    // uninstall listeners before fanout to avoid specious notifications
    for( IModelObject object: list) uninstallListeners( object);
    
    // uninstall next link
    IContext context = getListenerChain().getContext();
    IListenerChainLink nextListener = getNextListener();
    List<IModelObject> fanoutNodes = fanoutElement.query( context, list, null);
    if ( fanoutNodes.size() > 0) nextListener.incrementalUninstall( fanoutNodes);

    // do notification
    notifyRemove( list);

    // debug
    getListenerChain().debugIncrementalUninstall( list, getPathIndex()); 
  }

  /**
   * This method should be overridden to install the listeners in the model.
   * @param object The entry point into the model.
   */
  protected abstract void installListeners( IModelObject object);
  
  /**
   * This method should be overridden to uninstall the listeners in the model.
   * @param object The entry point into the model.
   */
  protected abstract void uninstallListeners( IModelObject object);
  
  /**
   * Returns the next listener in the chain.
   * @return Returns the next listener in the chain.
   */
  protected IListenerChainLink getNextListener()
  {
    IListenerChainLink[] links = chain.getLinks();
    return links[ chainIndex+1];
  }

  IPathElement fanoutElement;
}
