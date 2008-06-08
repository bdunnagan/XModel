/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xpath.function;

import dunnagan.bob.xmodel.xpath.expression.ExpressionException;
import dunnagan.bob.xmodel.xpath.expression.ExpressionListenerList;
import dunnagan.bob.xmodel.xpath.expression.IContext;
import dunnagan.bob.xmodel.xpath.expression.IExpression;

/**
 * An implementation of the X-Path string-length() function.
 */
public class StringLengthFunction extends Function
{
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.IExpression#getName()
   */
  public String getName()
  {
    return "string-length";
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.IExpression#getType()
   */
  public ResultType getType()
  {
    return ResultType.NUMBER;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#evaluateNumber(
   * dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  public double evaluateNumber( IContext context) throws ExpressionException
  {
    assertArgs( 0, 1);
    assertType( context, ResultType.STRING);
    
    IExpression arg0 = getArgument( 0);
    if ( arg0 != null)
    {
      return arg0.evaluateString( context).length();
    }
    else
    {
      return StringFunction.stringValue( context.getObject()).length();
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#notifyChange(
   * dunnagan.bob.xmodel.xpath.expression.IExpression, dunnagan.bob.xmodel.xpath.expression.IContext, 
   * java.lang.String, java.lang.String)
   */
  public void notifyChange( IExpression expression, IContext context, String newValue, String oldValue)
  {
    ExpressionListenerList listeners = getListeners();
    if ( listeners != null) 
    {
      double oldResult = oldValue.length();
      double newResult = newValue.length();
      if ( newResult != oldResult) listeners.notifyChange( this, context, newResult, oldResult);
    }
  }
}
