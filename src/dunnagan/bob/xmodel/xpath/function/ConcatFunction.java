/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xpath.function;

import java.util.List;

import dunnagan.bob.xmodel.xpath.expression.ExpressionException;
import dunnagan.bob.xmodel.xpath.expression.IContext;
import dunnagan.bob.xmodel.xpath.expression.IExpression;

/**
 * An implementation of the X-Path concat() function.
 */
public class ConcatFunction extends Function
{
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.IExpression#getName()
   */
  public String getName()
  {
    return "concat";
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.IExpression#getType()
   */
  public ResultType getType()
  {
    return ResultType.STRING;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#evaluateString
   * (dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  public String evaluateString( IContext context) throws ExpressionException
  {
    assertArgs( 0, Integer.MAX_VALUE);
    assertType( context, ResultType.STRING);

    StringBuffer buffer = new StringBuffer();
    List<IExpression> arguments = getArguments();
    for ( IExpression argument: arguments)
      buffer.append( argument.evaluateString( context));
    
    return buffer.toString();
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
    
    StringBuffer oldResult = new StringBuffer();
    StringBuffer newResult = new StringBuffer();
    List<IExpression> arguments = getArguments();
    for ( IExpression argument: arguments)
    {
      if ( argument == expression)
      {
        oldResult.append( oldValue);
        newResult.append( newValue);
      }
      else
      {
        try
        {
          String string = argument.evaluateString( context);
          oldResult.append( string);
          newResult.append( string);
        }
        catch( ExpressionException e)
        {
          parent.handleException( this, context, e);
        }
      }
    }
    
    parent.notifyChange( this, context, newResult.toString(), oldResult.toString());
  }
}
