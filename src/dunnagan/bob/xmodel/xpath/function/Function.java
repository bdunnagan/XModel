/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xpath.function;

import java.util.List;

import dunnagan.bob.xmodel.xpath.expression.Expression;
import dunnagan.bob.xmodel.xpath.expression.IExpression;

/**
 * Base class of all functions.  This class implements toString() to correctly print function.
 */
public abstract class Function extends Expression
{
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#toString()
   */
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append( getName());
    builder.append( '(');
    List<IExpression> arguments = getArguments();
    boolean useComma = arguments.size() > 1;
    for ( int i=0; i<arguments.size(); i++)
    {
      if ( i > 0 && useComma) builder.append( ", ");
      builder.append( getArgument( i).toString());
    }
    builder.append( ')');
    return builder.toString();
  }
}
