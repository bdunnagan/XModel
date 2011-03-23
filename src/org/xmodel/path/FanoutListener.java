/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * FanoutListener.java
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
import org.xmodel.IPathElement;
import org.xmodel.external.CachingException;
import org.xmodel.external.ConfiguredCachingPolicy;
import org.xmodel.external.ExternalReference;
import org.xmodel.external.IExternalReference;
import org.xmodel.external.UnboundedCache;
import org.xmodel.xpath.PathElement;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.ExpressionListener;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;


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
   * @see org.xmodel.path.IListenerChainLink#install(java.util.List)
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
   * @see org.xmodel.path.IListenerChainLink#uninstall(java.util.List)
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
   * @see org.xmodel.path.IListenerChainLink#incrementalUninstall(java.util.List)
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
  
  /* (non-Javadoc)
   * @see org.xmodel.path.ListenerChainLink#notifyDirty(org.xmodel.IModelObject, boolean)
   */
  @Override
  public void notifyDirty( IModelObject object, boolean dirty)
  {
//    if ( dirty)
//    {
//      // AbstractCachingPolicy is required to remove children when clearCache is called. Therefore, it is not
//      // necessary to call incrementalUninstall here.  In fact, the HACK code that follows the incrementalUninstall
//      // was put there because the incrementalUninstall causes an expression notification which prevents the
//      // incrementalInstall notification from being received.  But the incrementalUninstall is not necessary
//      // because the removeChildren call in clearCache performs that duty correctly.
//
//      // un-fanout (is this necessary?)
//      //incrementalUninstall( object);
//
//      // HACK: enable additional notification if this is part of an expression tree
//      //IListenerChain chain = getListenerChain();
//      //IPathListener listener = chain.getPathListener();
//      //if ( listener instanceof PathExpression)
//      //{
//      //  IExpression expression = ((PathExpression)listener).getRoot();
//      //  chain.getContext().markUpdate( expression);
//      //}
//      
//      // fanout
//      incrementalInstall( object);
//    }
  }

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
  
  public static void main( String[] args) throws Exception
  {
    IExpression expr = XPath.createExpression( "*");
    
    ExternalReference x = new ExternalReference( "x");
    x.setCachingPolicy( new CP());
    x.setDirty( true);
    
    StatefulContext context = new StatefulContext( x);
    expr.addNotifyListener( context, new ExpressionListener() {
      public void notifyAdd( IExpression expression, IContext context, List<IModelObject> nodes)
      {
        System.out.println( "notifyAdd");
      }
      public void notifyRemove( IExpression expression, IContext context, List<IModelObject> nodes)
      {
        System.out.println( "notifyRemove");
      }
    });
    
    System.out.println( "manually sync...");
    
    x.getChildren();
    
    System.out.println( "done");
  }
  
  private static class CP extends ConfiguredCachingPolicy
  {
    public CP()
    {
      super( new UnboundedCache());
    }
    
    protected void syncImpl( IExternalReference reference) throws CachingException
    {
      System.out.println( "syncing");
      reference.getCreateChild( "y");
    }
  }
}
