/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.xpath.function;

import org.xmodel.xpath.expression.ExpressionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

/**
 * An implementation of the X-Path starts-with() function.
 */
public class StartsWithFunction extends Function
{
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getName()
   */
  public String getName()
  {
    return "starts-with";
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
    assertArgs( 2, 2);
    assertType( context, ResultType.STRING);
    
    String string0 = getArgument( 0).evaluateString( context);
    String string1 = getArgument( 1).evaluateString( context);
    
    return string0.startsWith( string1);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyChange(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * java.lang.String, java.lang.String)
   */
  public void notifyChange( IExpression expression, IContext context, String newValue, String oldValue)
  {
    IExpression parent = getParent();
    if ( parent == null) return;

    IExpression arg0 = getArgument( 0);
    IExpression arg1 = getArgument( 1);
    try
    {
      if ( expression == arg0)
      {
        context.getModel().revert();
        boolean oldResult = oldValue.startsWith( arg1.evaluateString( context));
        context.getModel().restore();
        boolean newResult = newValue.startsWith( arg1.evaluateString( context));
        if ( newResult != oldResult) parent.notifyChange( this, context, newResult);
      }
      else
      {
        context.getModel().revert();
        boolean oldResult = arg0.evaluateString( context).startsWith( oldValue);
        context.getModel().restore();
        boolean newResult = arg0.evaluateString( context).startsWith( newValue);
        if ( newResult != oldResult) parent.notifyChange( this, context, newResult);
      }
    }
    catch( ExpressionException e)
    {
      parent.handleException( this, context, e);
    }
  }
}
