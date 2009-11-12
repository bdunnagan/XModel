/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ConcatFunction.java
 * 
 * Copyright 2009 Robert Arvin Dunnagan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.xmodel.xpath.function;

import java.util.List;
import org.xmodel.xpath.expression.ExpressionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;


/**
 * An implementation of the X-Path concat() function.
 */
public class ConcatFunction extends Function
{
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getName()
   */
  public String getName()
  {
    return "concat";
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getType()
   */
  public ResultType getType()
  {
    return ResultType.STRING;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#evaluateString
   * (org.xmodel.xpath.expression.IContext)
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
   * @see org.xmodel.xpath.expression.Expression#notifyChange(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
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
