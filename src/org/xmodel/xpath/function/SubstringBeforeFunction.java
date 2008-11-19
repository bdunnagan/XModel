/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.xpath.function;

import org.xmodel.xpath.expression.ExpressionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

/**
 * An implementation of the X-Path substring-before() function.
 */
public class SubstringBeforeFunction extends Function
{
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getName()
   */
  public String getName()
  {
    return "substring-before";
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getType()
   */
  public ResultType getType()
  {
    return ResultType.STRING;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#evaluateString(org.xmodel.xpath.expression.IContext)
   */
  public String evaluateString( IContext context) throws ExpressionException
  {
    assertArgs( 2, 2);
    assertType( context, ResultType.STRING);
    
    IExpression arg0 = getArgument( 0);
    IExpression arg1 = getArgument( 1);
    return substringBefore( arg0.evaluateString( context), arg1.evaluateString( context));
  }

  /**
   * Calculates the substring-before and returns the result.
   * @param string0 The first string.
   * @param string1 The second string.
   * @return Returns the result of the substring-before function.
   */
  private String substringBefore( String string0, String string1)
  {
    int index = string0.indexOf( string1);
    return (index >= 0)? string0.substring( 0, index): "";
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
        String oldResult = substringBefore( oldValue, arg1.evaluateString( context));
        context.getModel().restore();
        String newResult = substringBefore( newValue, arg1.evaluateString( context));
        if ( !newResult.equals( oldResult)) parent.notifyChange( this, context, newResult, oldResult);
      }
      else
      {
        context.getModel().revert();
        String oldResult = substringBefore( arg0.evaluateString( context), oldValue);
        context.getModel().restore();
        String newResult = substringBefore( arg0.evaluateString( context), newValue);
        if ( !newResult.equals( oldResult)) parent.notifyChange( this, context, newResult, oldResult);
      }
    }
    catch( ExpressionException e)
    {
      parent.handleException( this, context, e);
    }
  }
}
