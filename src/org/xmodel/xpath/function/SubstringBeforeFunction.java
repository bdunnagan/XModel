/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * SubstringBeforeFunction.java
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

import org.xmodel.GlobalSettings;
import org.xmodel.IModel;
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
    
    IModel model = GlobalSettings.getInstance().getModel();
    IExpression arg0 = getArgument( 0);
    IExpression arg1 = getArgument( 1);
    try
    {
      if ( expression == arg0)
      {
        model.revert();
        String oldResult = substringBefore( oldValue, arg1.evaluateString( context));
        model.restore();
        String newResult = substringBefore( newValue, arg1.evaluateString( context));
        if ( !newResult.equals( oldResult)) parent.notifyChange( this, context, newResult, oldResult);
      }
      else
      {
        model.revert();
        String oldResult = substringBefore( arg0.evaluateString( context), oldValue);
        model.restore();
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
