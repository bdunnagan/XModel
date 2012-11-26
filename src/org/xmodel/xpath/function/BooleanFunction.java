/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * BooleanFunction.java
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
package org.xmodel.xpath.function;

import java.util.List;
import org.xmodel.INode;
import org.xmodel.xpath.expression.ExpressionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;


/**
 * An implementation of the X-Path boolean() function.
 */
public class BooleanFunction extends Function
{
  public BooleanFunction()
  {
  }
  
  /**
   * Create a BooleanFunction with the given argument.
   * @param argument The argument expression.
   */
  public BooleanFunction( IExpression argument) 
  {
    addArgument( argument);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getName()
   */
  public String getName()
  {
    return "boolean";
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getType()
   */
  public ResultType getType()
  {
    return ResultType.BOOLEAN;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#evaluateBoolean(
   * org.xmodel.xpath.expression.IContext)
   */
  public boolean evaluateBoolean( IContext context) throws ExpressionException
  {
    assertArgs( 1, 1);
    IExpression argument = getArgument( 0);
    return argument.evaluateBoolean( context);
  }
  
  /**
   * Return the boolean value of the argument according to the X-Path 1.0 specification.
   * @param argument The argument to be converted.
   * @return Return the boolean value of the argument according to the X-Path 1.0 specification.
   */
  public static boolean booleanValue( List<INode> argument)
  {
    return argument.size() > 0;
  }
  
  /**
   * Return the boolean value of the argument according to the X-Path 1.0 specification.
   * @param argument The argument to be converted.
   * @return Return the boolean value of the argument according to the X-Path 1.0 specification.
   */
  public static boolean booleanValue( double argument)
  {
    return argument != 0;
  }
  
  /**
   * Return the boolean value of the argument according to the X-Path 1.0 specification.
   * @param argument The argument to be converted.
   * @return Return the boolean value of the argument according to the X-Path 1.0 specification.
   */
  public static boolean booleanValue( String argument)
  {
    return argument.length() > 0;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyAdd(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * java.util.List)
   */
  @Override
  public void notifyAdd( IExpression expression, IContext context, List<INode> nodes)
  {
    if ( getParent() != null) notifyChange( this, context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyRemove(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * java.util.List)
   */
  @Override
  public void notifyRemove( IExpression expression, IContext context, List<INode> nodes)
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
    if ( parent != null) parent.notifyChange( this, context, newValue);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyChange(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, double, double)
   */
  public void notifyChange( IExpression expression, IContext context, double newValue, double oldValue)
  {
    IExpression parent = getParent();
    if ( parent != null)
    {
      boolean oldResult = booleanValue( oldValue);
      boolean newResult = booleanValue( newValue);
      if ( newResult != oldResult) parent.notifyChange( this, context, newResult);
    }
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
      boolean oldResult = booleanValue( oldValue);
      boolean newResult = booleanValue( newValue);
      if ( newResult != oldResult) parent.notifyChange( this, context, newResult);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#requiresValueNotification(
   * org.xmodel.xpath.expression.IExpression)
   */
  @Override
  public boolean requiresValueNotification( IExpression argument)
  {
    return false;
  }
}
