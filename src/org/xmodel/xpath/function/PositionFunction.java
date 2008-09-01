/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.xpath.function;

import org.xmodel.xpath.expression.ExpressionException;
import org.xmodel.xpath.expression.IContext;

/**
 * An implementation of the X-Path position() function.
 */
public class PositionFunction extends Function
{
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getName()
   */
  public String getName()
  {
    return "position";
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getType()
   */
  public ResultType getType()
  {
    return ResultType.NUMBER;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#requiresContext()
   */
  @Override
  public boolean requiresOrdinalContext()
  {
    return true;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#evaluateNumber(
   * org.xmodel.xpath.expression.IContext)
   */
  public double evaluateNumber( IContext context) throws ExpressionException
  {
    assertArgs( 0, 0);
    return context.getPosition();
  }
}
