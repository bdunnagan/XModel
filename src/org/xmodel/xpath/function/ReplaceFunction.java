/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.xpath.function;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.ExpressionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;


/**
 * An implementation of the XPath 2.0 fn:replace function.  Regular expressions are implemented
 * using the Java Pattern class and may not conform to the XPath 2.0 specification.  The flags
 * parameter does roughly correspond to the XPath 2.0 specification.  All arguments must be of
 * type STRING.
 */
public class ReplaceFunction extends Function
{
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getName()
   */
  public String getName()
  {
    return "replace";
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
  @Override
  public String evaluateString( IContext context) throws ExpressionException
  {
    assertArgs( 3, 4);
    assertType( context, ResultType.STRING);
    
    Pattern pattern = getPattern( context);
    if ( pattern == null) return "";

    String input = getArgument( 0).evaluateString( context);
    Matcher matcher = pattern.matcher( input);
    return matcher.replaceAll( getArgument( 2).evaluateString( context));
  }

  /**
   * Returns a Pattern object for this expression.
   * @param context The context of the evaluation.
   * @return Returns the Pattern object.
   */
  private Pattern getPattern( IContext context) throws ExpressionException
  {
    try
    {
      if ( !init) 
      {
        String regex = getArgument( 1).evaluateString( context);
        pattern = Pattern.compile( regex, getFlags( context));
        init = true;
      }
      return pattern;
    }
    catch( PatternSyntaxException e)
    {
      throw new ExpressionException( this, "Invalid regular expression.", e);
    }
  }
  
  /**
   * Returns the flags of the pattern.
   * @param context The context of the evaluation.
   */
  private int getFlags( IContext context) throws ExpressionException
  {
    IExpression arg3 = getArgument( 3);
    if ( arg3 == null) return 0;
    
    int result = 0;
    String flags = arg3.evaluateString( context);
    if ( flags.contains( "s")) result |= Pattern.DOTALL;
    if ( flags.contains( "m")) result |= Pattern.MULTILINE;
    if ( flags.contains( "i")) result |= Pattern.CASE_INSENSITIVE;
    if ( flags.contains( "x")) result |= Pattern.COMMENTS;
    
    return result;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyChange(org.xmodel.xpath.expression.IExpression, 
   * org.xmodel.xpath.expression.IContext, java.lang.String, java.lang.String)
   */
  @Override
  public void notifyChange( IExpression expression, IContext context, String newValue, String oldValue)
  {
    if ( expression != getArgument( 0)) init = false;
    getParent().notifyChange( this, context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyChange(org.xmodel.xpath.expression.IExpression, 
   * org.xmodel.xpath.expression.IContext)
   */
  @Override
  public void notifyChange( IExpression expression, IContext context)
  {
    if ( expression != getArgument( 0)) init = false;
    getParent().notifyChange( this, context);
  }

  Pattern pattern;
  boolean init = false;
  
  public static void main( String[] args) throws Exception
  {
    IExpression expr = XPath.createExpression( "replace( 'abcabc', '(a)(.+)(a)', '$1$3')");
    System.out.println( "->"+expr.evaluateString( null));
  }
}
