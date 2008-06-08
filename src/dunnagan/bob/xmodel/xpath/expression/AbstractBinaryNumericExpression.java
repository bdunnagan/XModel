/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xpath.expression;

import java.util.List;

import dunnagan.bob.xmodel.IModelObject;

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
   * @see dunnagan.bob.xmodel.xpath.expression.IExpression#getType()
   */
  public ResultType getType()
  {
    return ResultType.NUMBER;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.IExpression#evaluateNumber(
   * dunnagan.bob.xmodel.xpath.expression.IContext)
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
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#notifyAdd(
   * dunnagan.bob.xmodel.xpath.expression.IExpression, dunnagan.bob.xmodel.xpath.expression.IContext, 
   * java.util.List)
   */
  @Override
  public void notifyAdd( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    if ( parent != null) notifyChange( this, context);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#notifyRemove(
   * dunnagan.bob.xmodel.xpath.expression.IExpression, dunnagan.bob.xmodel.xpath.expression.IContext, 
   * java.util.List)
   */
  @Override
  public void notifyRemove( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    if ( parent != null) notifyChange( this, context);
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#notifyChange(
   * dunnagan.bob.xmodel.xpath.expression.IExpression, dunnagan.bob.xmodel.xpath.expression.IContext, 
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
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#notifyChange(
   * dunnagan.bob.xmodel.xpath.expression.IExpression, dunnagan.bob.xmodel.xpath.expression.IContext, 
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
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#notifyChange(
   * dunnagan.bob.xmodel.xpath.expression.IExpression, dunnagan.bob.xmodel.xpath.expression.IContext, 
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
   * @see dunnagan.bob.xmodel.xpath.expression.IExpression#requiresValueNotification(
   * dunnagan.bob.xmodel.xpath.expression.IExpression)
   */
  @Override
  public boolean requiresValueNotification( IExpression argument)
  {
    return true;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.IExpression#notifyValue(java.util.List, 
   * dunnagan.bob.xmodel.IModelObject, java.lang.Object, java.lang.Object)
   */
  public void notifyValue( IExpression expression, IContext[] contexts, IModelObject object, Object newValue, Object oldValue)
  {
    for( IContext context: contexts) notifyChange( this, context);
  }
  
  LiteralExpression literal;
}
