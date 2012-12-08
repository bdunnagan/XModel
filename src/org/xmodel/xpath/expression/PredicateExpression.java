/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * PredicateExpression.java
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.xmodel.*;
import org.xmodel.log.Log;
import org.xmodel.xpath.variable.IVariableSource;

/**
 * An implementation of IExpression for X-Path 1.0 predicates.  This expression actually 
 * represents a predicate list.  It takes one or more arguments which are the expressions
 * that are evaluated boolean.
 */
public class PredicateExpression extends Expression implements IPredicate
{
  /**
   * Create a PredicateExpression which belongs to the specified path.
   * @param path The path (null for FilteredExpression).
   */
  public PredicateExpression( IPath path)
  {
    setParentPath( path);
  }
  
  /**
   * Set the path to which the predicate belongs.
   * @param path The path.
   */
  public void setParentPath( IPath path)
  {
    this.path = path;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IPredicate#filter(org.xmodel.xpath.expression.IContext, org.xmodel.IPath, int, java.util.List)
   */
  @Override
  public void filter( IContext parent, IPath candidatePath, int pathLength, List<IModelObject> nodes)
  {
    // TODO Auto-generated method stub
    
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IPredicate#bind(org.xmodel.xpath.expression.IContext, java.util.List)
   */
  @Override
  public void bind( IContext parent, List<IModelObject> nodes)
  {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see org.xmodel.IPredicate#bind(org.xmodel.xpath.expression.IContext, org.xmodel.IPath, int, java.util.List)
   */
  @Override
  public void bind( IContext parent, IPath candidatePath, int pathLength, List<IModelObject> nodes)
  {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see org.xmodel.IPredicate#unbind(org.xmodel.xpath.expression.IContext, java.util.List)
   */
  @Override
  public void unbind( IContext parent, List<IModelObject> nodes)
  {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see org.xmodel.IPredicate#unbind(org.xmodel.xpath.expression.IContext, org.xmodel.IPath, int, java.util.List)
   */
  @Override
  public void unbind( IContext parent, IPath candidatePath, int pathLength, List<IModelObject> nodes)
  {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getName()
   */
  public String getName()
  {
    return "predicate";
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getType()
   */
  public ResultType getType()
  {
    return ResultType.BOOLEAN;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#requiresContext()
   */
  @Override
  public boolean requiresOrdinalContext()
  {
    if ( !setup) 
    {
      if ( arguments != null)
      {
        for( IExpression argument: arguments)
          if ( argument.requiresOrdinalContext() || argument.getType() == ResultType.NUMBER)
          {
            requiresContext = true;
            break;
          }
      }
      else
      {
        requiresContext = false;
      }
    }
    return requiresContext;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#evaluateBoolean(
   * org.xmodel.xpath.expression.IContext)
   */
  public boolean evaluateBoolean( IContext context) throws ExpressionException
  {
    // FIXME: Cannot properly evaluate multiple predicates without examining complete candidate set
    for( IExpression arg: arguments)
    {
      if ( arg.getType( context) == ResultType.NUMBER)
      {
        int value = (int)arg.evaluateNumber( context);
        if ( value != context.getPosition()) return false;
      }
      else
      {
        if ( !arg.evaluateBoolean( context)) return false;
      }
    }
    
    return true;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#createSubtree(org.xmodel.xpath.expression.IContext, 
   * org.xmodel.IModelObjectFactory, org.xmodel.IChangeSet)
   */
  @Override
  public void createSubtree( IContext context, IModelObjectFactory factory, IChangeSet undo)
  {
    for( IExpression argument: getArguments())
      argument.createSubtree( context, factory, undo);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#getVariableSource()
   */
  @Override
  public IVariableSource getVariableSource()
  {
    if ( path != null) return path.getVariableSource();
    if ( parent != null) return parent.getVariableSource();
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IPredicate#addListener(org.xmodel.xpath.expression.IExpressionListener)
   */
  public void addListener( IExpressionListener listener)
  {
    if ( listeners == null) listeners = new ArrayList<IExpressionListener>( 1);
    listeners.add( listener);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IPredicate#removeListener(org.xmodel.xpath.expression.IExpressionListener)
   */
  public void removeListener( IExpressionListener listener)
  {
    if ( listeners != null) listeners.remove( listener);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IPredicate#getPredicateListeners()
   */
  public List<IExpressionListener> getPredicateListeners()
  {
    return listeners;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyAdd(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * java.util.List)
   */
  @Override
  public void notifyAdd( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    try
    {
      context.getModel().revert();
      List<IModelObject> oldNodes = expression.evaluateNodes( context);
      context.getModel().restore();
      if ( oldNodes.size() == 0) notifyChange( expression, context, true);
    }
    catch( ExpressionException e)
    {
      handleException( expression, context, e);
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
    try
    {
      List<IModelObject> newNodes = expression.evaluateNodes( context);
      if ( newNodes.size() == 0) notifyChange( expression, context, false);
    }
    catch( ExpressionException e)
    {
      handleException( expression, context, e);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyChange(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, boolean)
   */
  public void notifyChange( IExpression expression, IContext context, boolean newValue)
  {
    if ( parent != null) parent.notifyChange( this, context, newValue);
    if ( listeners != null) 
    {
      IExpressionListener[] array = listeners.toArray( new IExpressionListener[ 0]);
      for( IExpressionListener listener: array) listener.notifyChange( this, context, newValue);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyChange(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, double, double)
   */
  public void notifyChange( IExpression expression, IContext context, double newValue, double oldValue)
  {
    if ( parent != null) parent.notifyChange( this, context, newValue, oldValue);
    if ( listeners != null) 
    {
      IExpressionListener[] array = listeners.toArray( new IExpressionListener[ 0]);
      for( IExpressionListener listener: array) listener.notifyChange( this, context, newValue, oldValue);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyChange(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * java.lang.String, java.lang.String)
   */
  public void notifyChange( IExpression expression, IContext context, String newValue, String oldValue)
  {
    if ( newValue.length() == 0 && oldValue.length() > 0)
    {
      notifyChange( expression, context, false);
    }
    else if ( newValue.length() > 0 && oldValue.length() == 0)
    {
      notifyChange( expression, context, true);
    }
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.RootExpression#notifyChange(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext)
   */
  @Override
  public void notifyChange( IExpression expression, IContext context)
  {
    try
    {
      IModel model = context.getModel();
      switch( expression.getType( context))
      {
        case NODES:
        {
          // revert and reevaluate
          model.revert();
          Collection<IModelObject> oldNodes = expression.evaluateNodes( context);
  
          // restore and reevaluate
          model.restore();
          Collection<IModelObject> newNodes = expression.evaluateNodes( context);

          if ( oldNodes.size() == 0 && newNodes.size() > 0) notifyChange( this, context, true);
          if ( oldNodes.size() > 0 && newNodes.size() == 0) notifyChange( this, context, false);
        }
        break;
        
        case NUMBER:
        {
          // revert and reevaluate
          model.revert();
          double oldValue = expression.evaluateNumber( context);
  
          // restore and reevaluate
          model.restore();
          double newValue = expression.evaluateNumber( context);
          
          if ( newValue != oldValue) notifyChange( this, context, newValue, oldValue);
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
          
          if ( newValue != oldValue) notifyChange( this, context, newValue);
        }
        break;
        
        case STRING:
        {
          // revert and reevaluate
          model.revert();
          String oldValue = expression.evaluateString( context);
  
          // restore and reevaluate
          model.restore();
          String newValue = expression.evaluateString( context);
          
          if ( !newValue.equals( oldValue)) notifyChange( this, context, newValue, oldValue);
        }
        break;
      }      
    }
    catch( ExpressionException e)
    {
      handleException( this, context, e);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.RootExpression#requiresValueNotification(
   * org.xmodel.xpath.expression.IExpression)
   */
  @Override
  public boolean requiresValueNotification( IExpression argument)
  {
    // only the count of nodes is interesting
    return false;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#handleException(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * java.lang.Exception)
   */
  @Override
  public void handleException( IExpression expression, IContext context, Exception e)
  {
    if ( listeners == null)
    {
      System.err.println( "Expression Error: "+expression+", "+context);
      log.exception( e);
      return;
    }
    
    for( IExpressionListener listener: listeners)
      listener.handleException( this, context, e);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#clone()
   */
  public IPredicate clone()
  {
    return ((PredicateExpression)super.clone());
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#cloneOne()
   */
  @Override
  protected IExpression cloneOne()
  {
    return new PredicateExpression( path);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    for( IExpression arg: getArguments())
    {
      builder.append( '[');
      builder.append( arg.toString());
      builder.append( ']');
    }
    return builder.toString();
  }

  IPath path;
  boolean setup;
  boolean requiresContext;
  List<IExpressionListener> listeners;
  
  private static Log log = Log.getLog( "org.xmodel.xpath.expression");
}  
