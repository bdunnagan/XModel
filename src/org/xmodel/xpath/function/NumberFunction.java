/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.xpath.function;

import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.xpath.expression.ExpressionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;


/**
 * An implementation of the X-Path number() function.
 */
public class NumberFunction extends Function
{
  public NumberFunction()
  {
  }
  
  /**
   * Create a NumberFunction with the specified argument.
   * @param arg0 The argument to the function.
   */
  public NumberFunction( IExpression arg0)
  {
    addArgument( arg0);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getName()
   */
  public String getName()
  {
    return "number";
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
  public double evaluateNumber( IContext context) throws ExpressionException
  {
    assertArgs( 1, 1);
    
    IExpression argument = getArgument( 0);
    switch( argument.getType( context))
    {
      case NODES:   return numericValue( argument.evaluateNodes( context));
      case NUMBER:  return argument.evaluateNumber( context);   
      case STRING:  return numericValue( argument.evaluateString( context));     
      case BOOLEAN: return numericValue( argument.evaluateBoolean( context));   
    }
    return 0;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyAdd(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * java.util.List)
   */
  @Override
  public void notifyAdd( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    if ( getParent() != null) notifyChange( this, context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyRemove(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * java.util.List)
   */
  @Override
  public void notifyRemove( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    if ( getParent() != null) notifyChange( this, context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyChange(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, boolean)
   */
  public void notifyChange( IExpression expression, IContext context, boolean newValue)
  {
    IExpression parent = getParent();
    if ( parent != null)
    {
      double oldResult = numericValue( !newValue);
      double newResult = numericValue( newValue);
      parent.notifyChange( this, context, newResult, oldResult);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyChange(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, double, double)
   */
  public void notifyChange( IExpression expression, IContext context, double newValue, double oldValue)
  {
    IExpression parent = getParent();
    if ( parent != null) parent.notifyChange( this, context, newValue, oldValue);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyChange(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * java.lang.String, java.lang.String)
   */
  public void notifyChange( IExpression expression, IContext context, String newValue, String oldValue)
  {
    IExpression parent = getParent();
    if ( parent != null)
    {
      double oldResult = numericValue( oldValue);
      double newResult = numericValue( newValue);
      if ( newResult != oldResult) parent.notifyChange( this, context, newResult, oldResult);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#requiresValueNotification(
   * org.xmodel.xpath.expression.IExpression)
   */
  @Override
  public boolean requiresValueNotification( IExpression argument)
  {
    return true;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyValue(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * org.xmodel.IModelObject, java.lang.Object, java.lang.Object)
   */
  @Override
  public void notifyValue( IExpression expression, IContext[] contexts, IModelObject object, Object newValue, Object oldValue)
  {
    IExpression arg0 = getArgument( 0);
    for( IContext context: contexts)
    {
      IExpression parent = getParent();
      if ( parent != null)
      {
        try
        {
          List<IModelObject> nodes = arg0.evaluateNodes( context);
          if ( object.equals( nodes.get( 0))) 
          {
            double oldResult = (oldValue != null)? numericValue( oldValue.toString()): 0;
            double newResult = (newValue != null)? numericValue( newValue.toString()): 0;
            if ( newResult != oldResult) parent.notifyChange( this, context, newResult, oldResult);
          }
        }
        catch( ExpressionException e)
        {
          parent.handleException( this, context, e);
        }
      }
    }
  }

  /**
   * Return the numeric value of the argument according to the X-Path 1.0 specification.
   * @param nodes The node to be converted to a number.
   * @return Return the numeric value of the argument according to the X-Path 1.0 specification.
   */
  public static double numericValue( IModelObject node)
  {
    try
    {
      String string = StringFunction.stringValue( node);
      return Double.parseDouble( string);
    }
    catch( NumberFormatException e)
    {
      return 0;
    }
  }
  
  /**
   * Return the numeric value of the argument according to the X-Path 1.0 specification.
   * @param nodes The node-set to be converted to a number.
   * @return Return the numeric value of the argument according to the X-Path 1.0 specification.
   */
  public static double numericValue( List<IModelObject> nodes)
  {
    try
    {
      if ( nodes.size() == 0) return 0;
      String string = StringFunction.stringValue( nodes.get( 0));
      return Double.parseDouble( string);
    }
    catch( NumberFormatException e)
    {
      return 0;
    }
  }
  
  /**
   * Return the numeric value of the argument according to the X-Path 1.0 specification.
   * @param argument The argument to be converted.
   * @return Return the numeric value of the argument according to the X-Path 1.0 specification.
   */
  public static double numericValue( String argument)
  {
    try
    {
      return Double.parseDouble( argument);
    }
    catch( NumberFormatException e)
    {
      return 0;
    }
  }
  
  /**
   * Return the numeric value of the argument according to the X-Path 1.0 specification.
   * @param argument The argument to be converted.
   * @return Return the numeric value of the argument according to the X-Path 1.0 specification.
   */
  public static double numericValue( boolean argument)
  {
    return argument? 1: 0;
  }
}
