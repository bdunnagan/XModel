/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xpath.function;

import dunnagan.bob.xmodel.xpath.expression.ExpressionException;
import dunnagan.bob.xmodel.xpath.expression.IContext;
import dunnagan.bob.xmodel.xpath.expression.IExpression;

/**
 * An implementation of the X-Path contains() function.
 */
public class ContainsFunction extends Function
{
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.IExpression#getName()
   */
  public String getName()
  {
    return "contains";
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.IExpression#getType()
   */
  public ResultType getType()
  {
    return ResultType.BOOLEAN;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.IExpression#evaluateBoolean(
   * dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  public boolean evaluateBoolean( IContext context) throws ExpressionException
  {
    assertArgs( 2, 2);
    assertType( context, ResultType.STRING);
    
    String string0 = getArgument( 0).evaluateString( context);
    String string1 = getArgument( 1).evaluateString( context);
    
    return string0.contains( string1);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#notifyChange(
   * dunnagan.bob.xmodel.xpath.expression.IExpression, dunnagan.bob.xmodel.xpath.expression.IContext, 
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
        String string1 = arg1.evaluateString( context);
        boolean oldResult = oldValue.contains( string1);
        boolean newResult = newValue.contains( string1);
        if ( newResult != oldResult) parent.notifyChange( this, context, newResult);
      }
      else
      {
        String string0 = arg0.evaluateString( context);
        boolean oldResult = string0.contains( oldValue);        
        boolean newResult = string0.contains( newValue);
        if ( newResult != oldResult) parent.notifyChange( this, context, newResult);
      }
    }
    catch( ExpressionException e)
    {
      parent.handleException( this, context, e);
    }
  }
}
