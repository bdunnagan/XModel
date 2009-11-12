/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * NegateExpression.java
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

import org.xmodel.xpath.function.NumberFunction;

/**
 * An implementation of IExpression which represents an X-Path 1.0 negate expression.
 */
public class NegateExpression extends Expression
{
  public NegateExpression()
  {
  }
  
  /**
   * Create an NegateExpression with the given lhs and rhs expressions.
   * @param rhs The right-hand-side of the expression.
   */
  public NegateExpression( IExpression rhs)
  {
    addArgument( rhs);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getName()
   */
  public String getName()
  {
    return "negate";
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getType()
   */
  public ResultType getType()
  {
    return ResultType.NUMBER;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#addArgument(
   * org.xmodel.xpath.expression.IExpression)
   */
  public void addArgument( IExpression argument)
  {
    if ( argument.getType() != ResultType.NUMBER)
      argument = new NumberFunction( argument);
    super.addArgument( argument);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#evaluateNumber(
   * org.xmodel.xpath.expression.IContext)
   */
  public double evaluateNumber( IContext context) throws ExpressionException
  {
    IExpression rhs = getArgument( 0);
    return -(rhs.evaluateNumber( context));
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyChange(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * double, double)
   */
  public void notifyChange( IExpression expression, IContext context, double newValue, double oldValue)
  {
    if ( parent != null) parent.notifyChange( this, context, -newValue, -oldValue);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    IExpression rhs = getArgument( 0);
    return "-"+rhs.toString();
  }
}
