/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * PredicateGuard.java
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.xmodel.*;
import org.xmodel.xpath.expression.*;


/**
 * An implementation of IListenerChainLink which installs an IExpressionListener on the
 * PredicateExpression of the IPathElement to which it is associated. The PredicateGuard ensures
 * that the FanoutListener it guards is only installed on objects which lie on the predicated path.
 * <p>
 * The predicate listener is installed on the predicate of the previous IPathElement since the purpose
 * of the PredicateGuard is to guard the next IListenerChainLink.
 */
public class PredicateGuard extends ExpressionListener implements IListenerChainLink
{
  public PredicateGuard( IListenerChainLink guardedLink)
  {
    this.guardedLink = guardedLink;
    
    // create a partial path (0, pathIndex) and remove the predicate of the last element
    IPath path = getListenerChain().getPath();
    int pathIndex = getPathIndex();
    candidatePath = ModelAlgorithms.createCandidatePath( path, pathIndex-1);
  }

  protected PredicateGuard( PredicateGuard guard, IListenerChain chain)
  {
    this.guardedLink = guard.guardedLink.cloneOne( chain);
    this.candidatePath = guard.candidatePath;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.path.IListenerChainLink#bind(org.xmodel.xpath.expression.IContext)
   */
  public void bind( IContext context)
  {
    getPredicate().addListener( this);
  }

  /* (non-Javadoc)
   * @see org.xmodel.path.IListenerChainLink#unbind(org.xmodel.xpath.expression.IContext)
   */
  public void unbind( IContext context)
  {
    getPredicate().removeListener( this);
  }

  /* (non-Javadoc)
   * @see org.xmodel.path.IListenerChainLink#install(java.util.List)
   */
  public void install( List<IModelObject> list)
  {
    IListenerChain chain = getListenerChain();
    IContext parent = chain.getContext();
    List<IModelObject> nextLayer = new ArrayList<IModelObject>( list.size());
    IPredicate predicate = getPredicate();
    for ( int i=0; i<list.size(); i++)
    {
      IModelObject object = list.get( i);
      predicate.bind( new SubContext( parent, object, i+1, list.size()));
      if ( predicate.evaluate( parent, candidatePath, object)) nextLayer.add( object);
    }
    
    if ( nextLayer.size() > 0) guardedLink.install( nextLayer);
  }

  /* (non-Javadoc)
   * @see org.xmodel.path.IListenerChainLink#uninstall(java.util.List)
   */
  public void uninstall( List<IModelObject> list)
  {
    IContext parent = getListenerChain().getContext();
    List<IModelObject> nextLayer = new ArrayList<IModelObject>( list.size());
    IPredicate predicate = getPredicate();
    for ( int i=0; i<list.size(); i++)
    {
      IModelObject object = list.get( i);
      predicate.unbind( new SubContext( parent, object, i+1, list.size()));
      if ( predicate.evaluate( parent, candidatePath, object)) nextLayer.add( object);
    }
    
    if ( nextLayer.size() > 0) guardedLink.uninstall( nextLayer);
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
    // find candidates
    IListenerChain chain = getListenerChain();
    IContext bound = chain.getContext();
    List<IModelObject> candidates = candidatePath.query( bound, null);

    // find index of first object
    int start = Collections.indexOfSubList( candidates, list);
    int count = candidates.size();
    
    // add listeners
    IContext parent = chain.getContext();
    IPredicate predicate = getPredicate();
    for( int i=0; i<list.size(); i++)
      predicate.bind( new SubContext( parent, list.get( i), i+start+1, count));

    // evaluate predicate
    List<IModelObject> filtered = new ArrayList<IModelObject>( list.size());
    for( int i=0; i<list.size(); i++)
    {
      IModelObject object = list.get( i);
      if ( predicate.evaluate( parent, candidatePath, object))
        filtered.add( object);
    }
    
    if ( filtered.size() > 0) guardedLink.incrementalInstall( filtered);
  }

  /* (non-Javadoc)
   * @see org.xmodel.path.IListenerChainLink#incrementalUninstall(java.util.List)
   */
  public void incrementalUninstall( List<IModelObject> list)
  {
    // find candidates
    IListenerChain chain = getListenerChain();
    IContext bound = chain.getContext();
    IModel model = GlobalSettings.getInstance().getModel();
    model.revert();
    List<IModelObject> candidates = candidatePath.query( bound, null);
    model.restore();

    // find index of first object
    int start = Collections.indexOfSubList( candidates, list);
    int count = candidates.size();
    
    // remove listeners
    IContext parent = chain.getContext();
    IPredicate predicate = getPredicate();
    for( int i=0; i<list.size(); i++)
      predicate.unbind( new SubContext( parent, list.get( i), i+start+1, count));

    // evaluate predicate
    model.revert();
    List<IModelObject> filtered = new ArrayList<IModelObject>( list.size());
    for( int i=0; i<list.size(); i++)
    {
      IModelObject object = list.get( i);
      if ( predicate.evaluate( parent, candidatePath, object))
        filtered.add( object);
    }
    
    model.restore();
    if ( filtered.size() > 0) guardedLink.incrementalUninstall( filtered);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.path.IListenerChainLink#getListenerChain()
   */
  public IListenerChain getListenerChain()
  {
    return guardedLink.getListenerChain();
  }

  /* (non-Javadoc)
   * @see org.xmodel.path.IListenerChainLink#getPathIndex()
   */
  public int getPathIndex()
  {
    return guardedLink.getPathIndex();
  }

  /* (non-Javadoc)
   * @see org.xmodel.path.IListenerChainLink#cloneOne(org.xmodel.path.IListenerChain)
   */
  public IListenerChainLink cloneOne( IListenerChain chain)
  {
    return new PredicateGuard( this, chain);
  }

  /**
   * Returns the guarded predicate (the predicate of pathIndex).
   * @return Returns the guarded predicate.
   */
  public IPredicate getPredicate()
  {
    IPath path = getListenerChain().getPath();
    IPathElement pathElement = path.getPathElement( getPathIndex()-1);
    return pathElement.predicate();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpressionListener#notifyChange(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * double, double)
   */
  public void notifyChange( IExpression expression, IContext context, double newValue, double oldValue)
  {
    IContext pathContext = getListenerChain().getContext();
    if ( context.getParent() != pathContext) return;
    
    IContext parent = context.getParent();
    IListenerChain chain = getListenerChain();
    IListenerChainLink[] links = chain.getLinks();
    IListenerChainLink previous = links[ getPathIndex()];

    IModel model = GlobalSettings.getInstance().getModel();
    model.revert();
    List<IModelObject> oldCandidates = candidatePath.query( parent, null);
    
    model.restore();
    List<IModelObject> newCandidates = candidatePath.query( parent, null);

    // ensure that all SubContext instances have correct indices
    previous.uninstall( oldCandidates);
    previous.install( newCandidates);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpressionListener#notifyChange(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, boolean)
   */
  public void notifyChange( IExpression expression, IContext context, boolean newValue)
  {
    IContext pathContext = getListenerChain().getContext();
    if ( context.getParent() != pathContext) return;

    if ( newValue)
      guardedLink.incrementalInstall( context.getObject());
    else
      guardedLink.incrementalUninstall( context.getObject());
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpressionListener#requiresValueNotification()
   */
  public boolean requiresValueNotification()
  {
    return false;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    return "PredicateGuard: "+guardedLink.getListenerChain();
  }
  
  IListenerChainLink guardedLink;
  IPath candidatePath;
}
