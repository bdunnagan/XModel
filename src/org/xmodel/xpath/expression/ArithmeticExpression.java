/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ArithmeticExpression.java
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
 * An implementation of IExpression which represents an X-Path 1.0 arithmetic expression.
 */
public class ArithmeticExpression extends AbstractBinaryNumericExpression
{
  public enum Operator { ADD, SUB, MUL, DIV, MOD};
  
  /**
   * Create an ArithmeticExpression with the given operator.
   * @param operator The type of logical expression.
   */
  public ArithmeticExpression( Operator operator)
  {
    this.operator = operator;
  }
  
  /**
   * Create an ArithmeticExpression with the given operator, lhs and rhs expressions.
   * @param operator The type of logical expression.
   * @param lhs The left-hand-side of the expression.
   * @param rhs The right-hand-side of the expression.
   */
  public ArithmeticExpression( Operator operator, IExpression lhs, IExpression rhs)
  {
    this.operator = operator;
    addArgument( lhs);
    addArgument( rhs);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getName()
   */
  public String getName()
  {
    return "arithmetic";
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
   * @see org.xmodel.xpath.expression.AbstractBinaryNumericExpression#evaluate(
   * org.xmodel.xpath.expression.IContext, org.xmodel.xpath.expression.IExpression, 
   * org.xmodel.xpath.expression.IExpression)
   */
  public double evaluate( IContext context, IExpression lhs, IExpression rhs) throws ExpressionException
  {
    double lhv = lhs.evaluateNumber( context);
    double rhv = rhs.evaluateNumber( context);
    switch( operator)
    {
      case ADD: return lhv + rhv;
      case SUB: return lhv - rhv;
      case MUL: return lhv * rhv;
      case DIV: return lhv / rhv;
      case MOD: return lhv % rhv;
    }
    return 0;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    IExpression lhs = getArgument( 0);
    IExpression rhs = getArgument( 1);
    switch( operator)
    {
      case ADD: return lhs.toString()+" + "+rhs.toString();
      case SUB: return lhs.toString()+" - "+rhs.toString();
      case MUL: return lhs.toString()+" * "+rhs.toString();
      case DIV: return lhs.toString()+" / "+rhs.toString();
      case MOD: return lhs.toString()+" % "+rhs.toString();
    }
    return null;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#cloneOne()
   */
  @Override
  protected IExpression cloneOne()
  {
    return new ArithmeticExpression( operator);
  }
  
  Operator operator;
}
