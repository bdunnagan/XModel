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
  }

  protected PredicateGuard( PredicateGuard guard, IListenerChain chain)
  {
    this.guardedLink = guard.guardedLink.cloneOne( chain);
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
    
    IPredicate predicate = getPredicate();
    predicate.bind( parent, list);
    
    List<IModelObject> nextLayer = new ArrayList<IModelObject>( list);
    predicate.filter( parent, nextLayer);
    
    if ( nextLayer.size() > 0) guardedLink.install( nextLayer);
  }

  /* (non-Javadoc)
   * @see org.xmodel.path.IListenerChainLink#uninstall(java.util.List)
   */
  public void uninstall( List<IModelObject> list)
  {
    IContext parent = getListenerChain().getContext();
    
    IPredicate predicate = getPredicate();
    predicate.unbind( parent, list);
    
    List<IModelObject> nextLayer = new ArrayList<IModelObject>( list);
    predicate.filter( parent, nextLayer);
        
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
    IListenerChain chain = getListenerChain();
    IContext parent = chain.getContext();
    
    IPredicate predicate = getPredicate();
    predicate.bind( parent, chain.getPath(), getPathIndex(), list);
    
    List<IModelObject> nextLayer = new ArrayList<IModelObject>( list);
    predicate.filter( parent, chain.getPath(), getPathIndex(), nextLayer);
    
    if ( nextLayer.size() > 0) guardedLink.incrementalInstall( nextLayer);
  }

  /* (non-Javadoc)
   * @see org.xmodel.path.IListenerChainLink#incrementalUninstall(java.util.List)
   */
  public void incrementalUninstall( List<IModelObject> list)
  {
    //
    // Major retesting required for this change.
    //
    IModel model = getListenerChain().getContext().getModel();
    model.revert();
    
    try
    {
      IListenerChain chain = getListenerChain();
      IContext parent = chain.getContext();
      
      IPredicate predicate = getPredicate();
      predicate.unbind( parent, chain.getPath(), getPathIndex(), list);
      
      List<IModelObject> nextLayer = new ArrayList<IModelObject>( list);
      predicate.filter( parent, chain.getPath(), getPathIndex(), nextLayer);
      
      if ( nextLayer.size() > 0) guardedLink.incrementalUninstall( nextLayer);
    }
    finally
    {
      model.restore();
    }
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

    context.getModel().revert();
    List<IModelObject> oldCandidates = candidatePath.query( parent, null);
    
    context.getModel().restore();
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
}
