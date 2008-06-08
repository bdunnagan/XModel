/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xpath.expression;

import dunnagan.bob.xmodel.xpath.function.NumberFunction;

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
   * @see dunnagan.bob.xmodel.xpath.expression.IExpression#getName()
   */
  public String getName()
  {
    return "negate";
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.IExpression#getType()
   */
  public ResultType getType()
  {
    return ResultType.NUMBER;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#addArgument(
   * dunnagan.bob.xmodel.xpath.expression.IExpression)
   */
  public void addArgument( IExpression argument)
  {
    if ( argument.getType() != ResultType.NUMBER)
      argument = new NumberFunction( argument);
    super.addArgument( argument);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.IExpression#evaluateNumber(
   * dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  public double evaluateNumber( IContext context) throws ExpressionException
  {
    IExpression rhs = getArgument( 0);
    return -(rhs.evaluateNumber( context));
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#notifyChange(
   * dunnagan.bob.xmodel.xpath.expression.IExpression, dunnagan.bob.xmodel.xpath.expression.IContext, 
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
