/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * FilteredExpression.java
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
package org.xmodel.xpath.expression;

import java.util.*;
import org.xmodel.*;


/**
 * An implementation of IExpression which represents an X-Path 1.0 filtered expression.
 * FilteredExpression will forward update notifications from its primary expression or from any
 * predicate in its predicate list. The implementation of the evaluateBoolean method returns after
 * it finds the first node. Since most expressions occur inside of a predicate, which evaluates its
 * expression as a boolean, this should be much faster.
 */
public class FilteredExpression extends Expression
{
  /**
   * Create a filtered expression which filters the nodes in the node-set returned by
   * the lhs expression using the boolean result of the rhs predicate expression which
   * must be set separately as arguments.
   */
  public FilteredExpression()
  {
  }
  
  /**
   * Create a filtered expression which filters the nodes in the node-set returned by
   * the lhs expression using the boolean result of the rhs predicate expression.
   * @param lhs The expression returning the unfiltered node-set.
   * @param rhs The predicate expression which filters the node-set. 
   */
  public FilteredExpression( IExpression lhs, IExpression rhs)
  {
    addArgument( lhs);
    addArgument( rhs);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getName()
   */
  public String getName()
  {
    return "filtered";
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getType()
   */
  public ResultType getType()
  {
    return ResultType.NODES;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#evaluateNodes(
   * org.xmodel.xpath.expression.IContext)
   */
  public List<IModelObject> evaluateNodes( IContext context) throws ExpressionException
  {
    IExpression arg0 = getArgument( 0);
    IExpression arg1 = getArgument( 1);
    List<IModelObject> unfiltered = arg0.evaluateNodes( context);
    List<IModelObject> filtered = new ArrayList<IModelObject>( unfiltered.size());
    for ( int i=0; i<unfiltered.size(); i++)
    {
      IModelObject object = unfiltered.get( i);
      IContext filterContext = new SubContext( context, object, i+1, unfiltered.size());
      switch( arg1.getType( context))
      {
        case NODES:   
          filtered.addAll( arg1.evaluateNodes( filterContext));
          break;
        
        case BOOLEAN:
          if ( arg1.evaluateBoolean( filterContext))
            filtered.add( object);
          break;
      }
    }
    
    return filtered;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#evaluateBoolean(
   * org.xmodel.xpath.expression.IContext)
   */
  public boolean evaluateBoolean( IContext context) throws ExpressionException
  {
    IExpression arg0 = getArgument( 0);
    IExpression arg1 = getArgument( 1);
    List<IModelObject> unfiltered = arg0.evaluateNodes( context);
    for ( int i=0; i<unfiltered.size(); i++)
    {
      IModelObject object = unfiltered.get( i);
      IContext filterContext = new SubContext( context, object, i+1, unfiltered.size());
      switch( arg1.getType( context))
      {
        case NODES:   
          List<IModelObject> filtered = arg1.evaluateNodes( filterContext);
          if ( filtered.size() > 0) return true;
          break;
        
        case BOOLEAN:
          if ( arg1.evaluateBoolean( filterContext)) return true;
          break;
      }
    }
    
    return false;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#createSubtree(org.xmodel.xpath.expression.IContext, 
   * org.xmodel.IModelObjectFactory, org.xmodel.IChangeSet)
   */
  @Override
  public void createSubtree( IContext context, IModelObjectFactory factory, IChangeSet undo)
  {
    List<IModelObject> nodes = getArgument( 0).query( context, null);
    for( int i=0; i<nodes.size(); i++)
    {
      getArgument( 1).createSubtree( new SubContext( context, nodes.get( i), i+1, nodes.size()), factory, undo);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#bind(org.xmodel.xpath.expression.IContext)
   */
  public void bind( IContext context)
  {
    IExpression arg0 = getArgument( 0);
    IExpression arg1 = getArgument( 1);
    arg0.bind( context);

    List<IModelObject> nodes = arg0.evaluateNodes( context);
    for( int i=0; i<nodes.size(); i++)
    {
      IModelObject node = nodes.get( i);
      arg1.bind( new SubContext( context, node, i+1, nodes.size()));
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#unbind(org.xmodel.xpath.expression.IContext)
   */
  public void unbind( IContext context)
  {
    IExpression arg0 = getArgument( 0);
    IExpression arg1 = getArgument( 1);
    arg0.unbind( context);

    List<IModelObject> nodes = arg0.evaluateNodes( context);
    for( int i=0; i<nodes.size(); i++)
    {
      IModelObject node = nodes.get( i);
      arg1.unbind( new SubContext( context, node, i+1, nodes.size()));
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyAdd(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * java.util.List)
   */
  @Override
  public void notifyAdd( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    IExpression arg0 = getArgument( 0);
    IExpression arg1 = getArgument( 1);
    if ( expression == arg0)
    {
      List<IModelObject> lhsNodes = arg0.evaluateNodes( context);
      int index = Collections.indexOfSubList( lhsNodes, nodes);
      int count = lhsNodes.size();
      
      // optimize handling of input node-set changes
      if ( arg1.getType( context) == ResultType.BOOLEAN)
      {
        List<IModelObject> result = new ArrayList<IModelObject>( nodes.size());
        for( IModelObject node: nodes)
        {
          IContext filterContext = new SubContext(  context, node, (index++)+1, count);
          if ( arg1.evaluateBoolean( filterContext, false)) result.add( node);
          arg1.bind( filterContext);
        }
        
        // note that the ordering of binding vs. parent notification doesn't matter
        // because this optimization only applies to boolean expressions which do not
        // produce initial notifications.
        if ( result.size() > 0) parent.notifyAdd( this, context, result);
      }
      else
      {
        List<IModelObject> filterNodes = new ArrayList<IModelObject>();
        for( IModelObject node: nodes)
        {
          IContext filterContext = new SubContext(  context, node, (index++)+1, count);
          arg1.bind( filterContext);
          filterNodes.addAll( arg1.evaluateNodes( filterContext));
        }
        if ( filterNodes.size() > 0) parent.notifyAdd( this, context, filterNodes);
      }
    }
    else if ( expression == arg1)
    {
      // forward rhs notification to parent
      parent.notifyAdd( this, context.getParent(), nodes);
    }
    else
    {
      throw new IllegalStateException( "Notification from expression which is not an argument.");
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyRemove(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * java.util.List)
   */
  @Override
  public void notifyRemove( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    IModel model = GlobalSettings.getInstance().getModel();
    IExpression arg0 = getArgument( 0);
    IExpression arg1 = getArgument( 1);
    if ( expression == arg0)
    {
      model.revert();
      List<IModelObject> lhsNodes = arg0.evaluateNodes( context);
      
      model.restore();
      int index = Collections.indexOfSubList( lhsNodes, nodes);
      int count = lhsNodes.size();
      
      // optimize handling of input node-set changes
      if ( arg1.getType( context) == ResultType.BOOLEAN)
      {
        model.revert();
        List<IModelObject> result = new ArrayList<IModelObject>( nodes.size());
        for( IModelObject node: nodes)
        {
          IContext filterContext = new SubContext( context, node, (index++)+1, count);
          if ( arg1.evaluateBoolean( filterContext, false)) result.add( node);
          arg1.unbind( filterContext);
        }
        
        // note that the ordering of binding vs. parent notification doesn't matter
        // because this optimization only applies to boolean expressions which do not
        // produce initial notifications.
        model.restore();
        if ( result.size() > 0) parent.notifyRemove( this, context, result);
      }
      else
      {
        model.revert();
        List<IModelObject> filterNodes = new ArrayList<IModelObject>();
        for( IModelObject node: nodes)
        {
          IContext filterContext = new SubContext(  context, node, (index++)+1, count);
          arg1.unbind( filterContext);
          filterNodes.addAll( arg1.evaluateNodes( filterContext));
        }
        model.restore();
        if ( filterNodes.size() > 0) parent.notifyRemove( this, context, filterNodes);
      }
    }
    else if ( expression == arg1)
    {
      // forward rhs notification to parent
      parent.notifyRemove( this, context.getParent(), nodes);
    }
    else
    {
      throw new IllegalStateException( "Notification from expression which is not an argument.");
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyChange(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, boolean)
   */
  @Override
  public void notifyChange( IExpression expression, IContext context, boolean newValue)
  {
    List<IModelObject> list = Collections.singletonList( context.getObject());
    if ( newValue)
      parent.notifyAdd( this, context.getParent(), list);
    else
      parent.notifyRemove( this, context.getParent(), list);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyChange(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext)
   */
  @Override
  public void notifyChange( IExpression expression, IContext context)
  {
    IModel model = GlobalSettings.getInstance().getModel();
    switch( expression.getType( context))
    {
      case NODES:
      {
        // revert and reevaluate
        model.revert();
        Collection<IModelObject> oldNodes = expression.evaluateNodes( context);
        if ( oldNodes.size() > 3) oldNodes = new LinkedHashSet<IModelObject>( oldNodes);

        // restore and reevaluate
        model.restore();
        Collection<IModelObject> newNodes = expression.evaluateNodes( context);
        if ( newNodes.size() > 3) newNodes = new LinkedHashSet<IModelObject>( newNodes);

        // notify nodes removed
        List<IModelObject> removedSet = new ArrayList<IModelObject>( newNodes.size());
        for( IModelObject node: oldNodes) if ( !newNodes.contains( node)) removedSet.add( node);
        if ( removedSet.size() > 0) notifyRemove( expression, context, removedSet);
        
        // notify nodes added
        List<IModelObject> addedSet = new ArrayList<IModelObject>( newNodes.size());
        for( IModelObject node: newNodes) if ( !oldNodes.contains( node)) addedSet.add( node);
        if ( addedSet.size() > 0) notifyAdd( expression, context, addedSet);
      }
      break;
              
      case BOOLEAN:
      {
        // revert and reevaluate
        model.revert();
        boolean oldValue = expression.evaluateBoolean( context);

        // restore and reevaluate
        model.restore();
        boolean newValue = expression.evaluateBoolean( context);
        
        if ( newValue != oldValue) notifyChange( expression, context, newValue);
      }
      break;
    }      
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#requiresValueNotification(
   * org.xmodel.xpath.expression.IExpression)
   */
  @Override
  public boolean requiresValueNotification( IExpression argument)
  {
    IExpression arg0 = getArgument( 0);
    IExpression arg1 = getArgument( 1);
    
    // need value notification if type of arg1 is undefined
    ResultType type1 = arg1.getType();
    if ( type1 == ResultType.UNDEFINED) return true;
    
    // need value notification from arg0 so arg1 can examine value
    if ( argument == arg0 && type1 == ResultType.BOOLEAN) return true;
    
    // need value notification from arg1 if arg1 returns nodes and parent needs value notification
    if ( argument == arg1 && type1 == ResultType.NODES && parent.requiresValueNotification( this)) return true;
    return false;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#notifyValue(
   * org.xmodel.xpath.expression.IContext[], org.xmodel.IModelObject, 
   * java.lang.Object, java.lang.Object)
   */
  @Override
  public void notifyValue( IExpression expression, IContext[] contexts, IModelObject object, Object newValue, Object oldValue)
  {
    IModel model = GlobalSettings.getInstance().getModel();
    
    // unwind context
    if ( expression == getArgument( 1))
    {
      IContext[] parents = new IContext[ contexts.length];
      for( int i=0; i<parents.length; i++) parents[ i] = contexts[ i].getParent();
      contexts = parents;
    }

    // notify
    if ( expression.getType( contexts[ 0]) == ResultType.BOOLEAN)
    {
      model.revert();
      for( IContext context: contexts) unbind( context);
      
      model.restore();
      for( IContext context: contexts) bind( context);
  
      // reevaluate and notify listeners
      for( IContext context: contexts) notifyChange( this, context);
    }
    else
    {
      parent.notifyValue( this, contexts, object, newValue, oldValue);
    }
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    IExpression lhs = getArgument( 0);
    IExpression rhs = getArgument( 1);
    if ( rhs.getType() == ResultType.BOOLEAN)
    {
      return "("+lhs.toString()+")"+rhs.toString();
    }
    else if ( rhs instanceof PathExpression)
    {
      PathExpression pathExpression = (PathExpression)rhs;
      IPath path = pathExpression.getPath();
      IPathElement element = path.getPathElement( 0);
      if ( element.hasAxis( IAxis.DESCENDANT))
        return "("+lhs.toString()+")//"+rhs.toString();
      else
        return "("+lhs.toString()+")/"+rhs.toString();
    }
    else
    {
      return "("+lhs.toString()+")/("+rhs.toString()+")";
    }
  }
}