/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * AbstractBinaryNumericExpression.java
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
 * An extension of Expression for binary numeric expressions. This class provides a base
 * implementation of incremental notification for these types of expressions. It is assumed
 * that the expression requires value notification from its node-set arguments.
 * <p>
 * Incremental notification will work even if the arguments are being implicitly cast (as 
 * with the EqualityExpression). In this case, change notifications for all expression types
 * may be received.
 */
public abstract class AbstractBinaryNumericExpression extends Expression
{
  protected AbstractBinaryNumericExpression()
  {
    literal = new LiteralExpression();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getType()
   */
  public ResultType getType()
  {
    return ResultType.NUMBER;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#evaluateNumber(
   * org.xmodel.xpath.expression.IContext)
   */
  @Override
  public double evaluateNumber( IContext context) throws ExpressionException
  {
    return evaluate( context, getArgument( 0), getArgument( 1));    
  }

  /**
   * Evaluate this expression in the given context with the specified argument expressions.
   * @param context The context.
   * @param lhs The left-hand-side argument.
   * @param rhs The right-hand-side argument.
   * @return Returns the result.
   */
  protected abstract double evaluate( IContext context, IExpression lhs, IExpression rhs)
  throws ExpressionException;
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyAdd(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * java.util.List)
   */
  @Override
  public void notifyAdd( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    if ( parent != null) notifyChange( this, context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyRemove(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * java.util.List)
   */
  @Override
  public void notifyRemove( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    if ( parent != null) notifyChange( this, context);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyChange(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * boolean)
   */
  public void notifyChange( IExpression expression, IContext context, boolean newValue)
  {
    if ( parent == null) return;

    try
    {
      IExpression lhs = getArgument( 0);
      IExpression rhs = getArgument( 1);
      if ( expression == lhs)
      {
        context.getModel().revert();
        literal.setValue( !newValue);
        double oldResult = evaluate( context, literal, rhs);
               
        context.getModel().restore();
        literal.setValue( newValue);
        double newResult = evaluate( context, literal, rhs);
        
        if ( oldResult != newResult) parent.notifyChange( this, context, newResult, oldResult);
      }
      else
      {
        context.getModel().revert();
        literal.setValue( !newValue);
        double oldResult = evaluate( context, lhs, literal);
               
        context.getModel().restore();
        literal.setValue( newValue);
        double newResult = evaluate( context, lhs, literal);
        
        if ( oldResult != newResult) parent.notifyChange( this, context, newResult, oldResult);
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
    if ( parent == null) return;

    try
    {
      IExpression lhs = getArgument( 0);
      IExpression rhs = getArgument( 1);
      if ( expression == lhs)
      {
        context.getModel().revert();
        literal.setValue( oldValue);
        double oldResult = evaluate( context, literal, rhs);
               
        context.getModel().restore();
        literal.setValue( newValue);
        double newResult = evaluate( context, literal, rhs);
        
        if ( oldResult != newResult) parent.notifyChange( this, context, newResult, oldResult);
      }
      else
      {
        context.getModel().revert();
        literal.setValue( oldValue);
        double oldResult = evaluate( context, lhs, literal);
               
        context.getModel().restore();
        literal.setValue( newValue);
        double newResult = evaluate( context, lhs, literal);
        
        if ( oldResult != newResult) parent.notifyChange( this, context, newResult, oldResult);
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
    if ( parent == null) return;

    try
    {
      IExpression lhs = getArgument( 0);
      IExpression rhs = getArgument( 1);
      if ( expression == lhs)
      {
        context.getModel().revert();
        literal.setValue( oldValue);
        double oldResult = evaluate( context, literal, rhs);
               
        context.getModel().restore();
        literal.setValue( newValue);
        double newResult = evaluate( context, literal, rhs);
        
        if ( oldResult != newResult) parent.notifyChange( this, context, newResult, oldResult);
      }
      else
      {
        context.getModel().revert();
        literal.setValue( oldValue);
        double oldResult = evaluate( context, lhs, literal);
               
        context.getModel().restore();
        literal.setValue( newValue);
        double newResult = evaluate( context, lhs, literal);
        
        if ( oldResult != newResult) parent.notifyChange( this, context, newResult, oldResult);
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
    for( IContext context: contexts) notifyChange( this, context);
  }
  
  LiteralExpression literal;
}
