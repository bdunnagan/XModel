/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * AbstractBinaryBooleanExpression.java
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

import java.util.List;
import org.xmodel.IModelObject;


/**
 * An extension of Expression for binary boolean expressions. This class provides a base
 * implementation of incremental notification for these types of expressions. It is assumed
 * that the expression requires value notification from its node-set arguments.
 * <p>
 * Incremental notification will work even if the arguments are being implicitly cast (as 
 * with the EqualityExpression). In this case, change notifications for all expression types
 * may be received.
 */
public abstract class AbstractBinaryBooleanExpression extends Expression
{
  protected AbstractBinaryBooleanExpression()
  {
    literal = new LiteralExpression();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getType()
   */
  public ResultType getType()
  {
    return ResultType.BOOLEAN;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#evaluateBoolean(
   * org.xmodel.xpath.expression.IContext)
   */
  @Override
  public boolean evaluateBoolean( IContext context) throws ExpressionException
  {
    return evaluate( context, getArgument( 0), getArgument( 1));    
  }

  /**
   * Evaluate this expression in the given context with the specified argument expressions.
   * @param context The context.
   * @param lhs The left-hand-side argument.
   * @param rhs The right-hand-side argument.
   * @return Returns the comparison result.
   */
  protected abstract boolean evaluate( IContext context, IExpression lhs, IExpression rhs) throws ExpressionException;
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyAdd(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * java.util.List)
   */
  @Override
  public void notifyAdd( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    evaluateChange( context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyRemove(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * java.util.List)
   */
  @Override
  public void notifyRemove( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    evaluateChange( context);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyChange(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * boolean)
   */
  public void notifyChange( IExpression expression, IContext context, boolean newValue)
  {
    try
    {
      IExpression lhs = getArgument( 0);
      IExpression rhs = getArgument( 1);
      if ( expression == lhs)
      {
        context.getModel().revert();
        literal.setValue( !newValue);
        boolean oldResult = evaluate( context, literal, rhs);
               
        context.getModel().restore();
        literal.setValue( newValue);
        boolean newResult = evaluate( context, literal, rhs);
        
        if ( oldResult != newResult) parent.notifyChange( this, context, newResult);
      }
      else
      {
        context.getModel().revert();
        literal.setValue( !newValue);
        boolean oldResult = evaluate( context, lhs, literal);
               
        context.getModel().restore();
        literal.setValue( newValue);
        boolean newResult = evaluate( context, lhs, literal);
        
        if ( oldResult != newResult) parent.notifyChange( this, context, newResult);
      }
    }
    catch( ExpressionException e)
    {
      parent.handleException( this, context, e);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyChange(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * double, double)
   */
  public void notifyChange( IExpression expression, IContext context, double newValue, double oldValue)
  {
    try
    {
      IExpression lhs = getArgument( 0);
      IExpression rhs = getArgument( 1);
      if ( expression == lhs)
      {
        context.getModel().revert();
        literal.setValue( oldValue);
        boolean oldResult = evaluate( context, literal, rhs);
               
        context.getModel().restore();
        literal.setValue( newValue);
        boolean newResult = evaluate( context, literal, rhs);
        
        if ( oldResult != newResult) parent.notifyChange( this, context, newResult);
      }
      else
      {
        context.getModel().revert();
        literal.setValue( oldValue);
        boolean oldResult = evaluate( context, lhs, literal);
               
        context.getModel().restore();
        literal.setValue( newValue);
        boolean newResult = evaluate( context, lhs, literal);
        
        if ( oldResult != newResult) parent.notifyChange( this, context, newResult);
      }
    }
    catch( ExpressionException e)
    {
      parent.handleException( this, context, e);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyChange(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * java.lang.String, java.lang.String)
   */
  public void notifyChange( IExpression expression, IContext context, String newValue, String oldValue)
  {
    try
    {
      IExpression lhs = getArgument( 0);
      IExpression rhs = getArgument( 1);
      if ( expression == lhs)
      {
        context.getModel().revert();
        literal.setValue( oldValue);
        boolean oldResult = evaluate( context, literal, rhs);
               
        context.getModel().restore();
        literal.setValue( newValue);
        boolean newResult = evaluate( context, literal, rhs);
        
        if ( oldResult != newResult) parent.notifyChange( this, context, newResult);
      }
      else
      {
        context.getModel().revert();
        literal.setValue( oldValue);
        boolean oldResult = evaluate( context, lhs, literal);
               
        context.getModel().restore();
        literal.setValue( newValue);
        boolean newResult = evaluate( context, lhs, literal);
        
        if ( oldResult != newResult) parent.notifyChange( this, context, newResult);
      }
    }
    catch( ExpressionException e)
    {
      parent.handleException( this, context, e);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#requiresValueNotification(
   * org.xmodel.xpath.expression.IExpression)
   */
  @Override
  public boolean requiresValueNotification( IExpression argument)
  {
    return true;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#notifyValue(java.util.List, 
   * org.xmodel.IModelObject, java.lang.Object, java.lang.Object)
   */
  public void notifyValue( IExpression expression, IContext[] contexts, IModelObject object, Object newValue, Object oldValue)
  {
    evaluateChange( contexts[ 0]);
  }
  
  /**
   * Reevaluate both sides of this expression before and after reverting the model
   * and send targeted notification to the parent expression. This is a workaround
   * for the AND expressions which can interact badly with the current notification
   * semantics (see BUG #58).
   * @param context The context.
   */
  private void evaluateChange( IContext context)
  {
    IExpression lhs = getArgument( 0);
    IExpression rhs = getArgument( 1);
    
    context.getModel().revert();
    boolean oldResult = evaluate( context, lhs, rhs);
           
    context.getModel().restore();
    boolean newResult = evaluate( context, lhs, rhs);
    
    if ( oldResult != newResult) parent.notifyChange( this, context, newResult);
  }
  
  private LiteralExpression literal;
}
