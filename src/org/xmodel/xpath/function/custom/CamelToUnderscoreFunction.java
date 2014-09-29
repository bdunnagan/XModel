package org.xmodel.xpath.function.custom;

import org.xmodel.xpath.expression.ExpressionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.function.Function;

/**
 * Convert from character case of argument from camel-back to underscore.
 */
public class CamelToUnderscoreFunction extends Function
{
  public final static String name = "camel-to-underscore";
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getName()
   */
  @Override
  public String getName()
  {
    return name;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getType()
   */
  @Override
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
    assertArgs( 1, 1);
    
    String text = getArgument( 0).evaluateString( context);
    return convertCase( text);
  }
  
  public static String convertCase( String text)
  {
    StringBuilder sb = new StringBuilder();
    for( int i=0; i<text.length(); i++)
    {
      char c = text.charAt( i);
      if ( Character.isUpperCase( c))
      {
        sb.append( '_');
        c = Character.toLowerCase( c);
      }
      sb.append( c);
    }
    
    return sb.toString();
  }
  
  public static void main( String[] args) throws Exception
  {
    System.out.println( convertCase( "max"));
  }
}
