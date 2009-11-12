/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * StartsWithFunction.java
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
