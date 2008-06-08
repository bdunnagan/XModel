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
 * An implementation of the X-Path translate() function.
 */
public class TranslateFunction extends Function
{
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.IExpression#getName()
   */
  public String getName()
  {
    return "translate";
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.IExpression#getType()
   */
  public ResultType getType()
  {
    return ResultType.STRING;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#evaluateString(
   * dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  public String evaluateString( IContext context) throws ExpressionException
  {
    assertArgs( 3, 3);
    assertType( context, ResultType.STRING);
    
    String string0 = getArgument( 0).evaluateString( context);
    String string1 = getArgument( 1).evaluateString( context);
    String string2 = getArgument( 2).evaluateString( context);
    return translate( string0, string1, string2);
  }

  /**
   * Perform the translate function.
   * @param string0 The string to be translated.
   * @param string1 The translate <i>from</i> characters.
   * @param string2 The translate <i>to</i> characters.
   * @return Returns the result of the translate function.
   */
  private String translate( String string0, String string1, String string2)
  {
    StringBuilder string = new StringBuilder( string0);
    int length1 = string1.length();
    int length2 = string2.length();
    int length = (length1 < length2)? length1: length2;
    for ( int i=0; i<string.length(); i++)
    {
      for ( int j=0; j<length; j++)
        if ( string.charAt( i) == string1.charAt( j))
          string.setCharAt( i, string2.charAt( j));
    }
    return string.toString();
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
    IExpression arg2 = getArgument( 2);
    String string0, string1, string2;
    try
    {
      if ( expression == arg0)
      {
        context.getModel().revert();
        string1 = arg1.evaluateString( context);
        string2 = arg2.evaluateString( context);
        String oldResult = translate( oldValue, string1, string2);

        context.getModel().restore();
        string1 = arg1.evaluateString( context);
        string2 = arg2.evaluateString( context);
        String newResult = translate( newValue, string1, string2);
        
        if ( !newResult.equals( oldResult)) parent.notifyChange( this, context, newResult, oldResult);
      }
      else if ( expression == arg1)
      {
        context.getModel().revert();
        string0 = arg0.evaluateString( context);
        string2 = arg2.evaluateString( context);
        String oldResult = translate( string0, oldValue, string2);

        context.getModel().restore();
        string0 = arg0.evaluateString( context);
        string2 = arg2.evaluateString( context);
        String newResult = translate( string0, newValue, string2);
        
        if ( !newResult.equals( oldResult)) parent.notifyChange( this, context, newResult, oldResult);
      }
      else
      {
        context.getModel().revert();
        string0 = arg0.evaluateString( context);
        string1 = arg1.evaluateString( context);
        String oldResult = translate( string0, string1, oldValue);

        context.getModel().restore();
        string0 = arg0.evaluateString( context);
        string1 = arg1.evaluateString( context);
        String newResult = translate( string0, string1, newValue);
        
        if ( !newResult.equals( oldResult)) parent.notifyChange( this, context, newResult, oldResult);
      }
    }
    catch( ExpressionException e)
    {
      parent.handleException( this, context, e);
    }
  }
}
