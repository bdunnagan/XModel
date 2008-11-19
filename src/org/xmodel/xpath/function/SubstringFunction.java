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
 * An implementation of the X-Path substring() function.
 */
public class SubstringFunction extends Function
{
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getName()
   */
  public String getName()
  {
    return "substring";
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getType()
   */
  public ResultType getType()
  {
    return ResultType.STRING;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#evaluateString(
   * org.xmodel.xpath.expression.IContext)
   */
  public String evaluateString( IContext context) throws ExpressionException
  {
    assertArgs( 2, 3);
    assertType( context, 0, ResultType.STRING);
    assertType( context, 1, ResultType.NUMBER);
    assertType( context, 2, ResultType.NUMBER);
    
    IExpression arg0 = getArgument( 0);
    IExpression arg1 = getArgument( 1);
    IExpression arg2 = getArgument( 2);

    String string = arg0.evaluateString( context);
    int start = (int)arg1.evaluateNumber( context);
    int end = (arg2 != null)? end = (int)arg2.evaluateNumber( context): -1;
    return substring( string, start, end);
  }

  /**
   * Find the substring of the specified string.
   * @param string The input string.
   * @param start The start index of the substring.
   * @param end The end index of the substring (or -1).
   * @return Returns the substring of the specified string.
   */
  private String substring( String string, int start, int end)
  {
    if ( end < 0)
    {
      return string.substring( start);
    }
    else
    {
      return string.substring( start, end);
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
    if ( parent == null) return;
    
    IExpression arg1 = getArgument( 1);
    IExpression arg2 = getArgument( 2);
    int start, end;
    try
    {
      context.getModel().revert();
      start = (int)arg1.evaluateNumber( context);
      end = (arg2 != null)? end = (int)arg2.evaluateNumber( context): -1;
      String oldResult = substring( oldValue, start, end);

      context.getModel().restore();
      start = (int)arg1.evaluateNumber( context);
      end = (arg2 != null)? end = (int)arg2.evaluateNumber( context): -1;
      String newResult = substring( newValue, start, end);
      
      if ( !newResult.equals( oldResult)) parent.notifyChange( this, context, newResult, oldResult);
    }
    catch( ExpressionException e)
    {
      parent.handleException( this, context, e);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyChange(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, double, double)
   */
  public void notifyChange( IExpression expression, IContext context, double newValue, double oldValue)
  {
    IExpression parent = getParent();
    if ( parent == null) return;
      
    IExpression arg0 = getArgument( 0);
    IExpression arg1 = getArgument( 1);
    IExpression arg2 = getArgument( 2);
    String string;
    int start, end;
    try
    {
      if ( expression == arg1)
      {
        context.getModel().revert();
        string = arg0.evaluateString( context);
        end = (arg2 != null)? end = (int)arg2.evaluateNumber( context): -1;
        String oldResult = substring( string, (int)oldValue, end);

        context.getModel().restore();
        string = arg0.evaluateString( context);
        end = (arg2 != null)? end = (int)arg2.evaluateNumber( context): -1;
        String newResult = substring( string, (int)newValue, end);
        
        if ( !newResult.equals( oldResult)) parent.notifyChange( this, context, newResult, oldResult);
      }
      else
      {
        context.getModel().revert();
        string = arg0.evaluateString( context);
        start = (int)arg1.evaluateNumber( context);
        String oldResult = substring( string, start, (int)oldValue);
        
        context.getModel().restore();
        string = arg0.evaluateString( context);
        start = (int)arg1.evaluateNumber( context);
        String newResult = substring( string, start, (int)newValue);
        
        if ( !newResult.equals( oldResult)) parent.notifyChange( this, context, newResult, oldResult);
      }
    }
    catch( ExpressionException e)
    {
      parent.handleException( this, context, e);
    }
  }
}
