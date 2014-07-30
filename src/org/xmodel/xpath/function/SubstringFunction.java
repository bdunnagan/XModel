/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * SubstringFunction.java
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
    if ( start < 0) return string;
    
    if ( end < 0)
    {
      return string.substring( start);
    }
    else
    {
      if ( end > string.length()) end = string.length();
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
    
    IModel model = GlobalSettings.getInstance().getModel();
    IExpression arg1 = getArgument( 1);
    IExpression arg2 = getArgument( 2);
    int start, end;
    try
    {
      model.revert();
      start = (int)arg1.evaluateNumber( context);
      end = (arg2 != null)? end = (int)arg2.evaluateNumber( context): -1;
      String oldResult = substring( oldValue, start, end);

      model.restore();
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
      
    IModel model = GlobalSettings.getInstance().getModel();
    IExpression arg0 = getArgument( 0);
    IExpression arg1 = getArgument( 1);
    IExpression arg2 = getArgument( 2);
    String string;
    int start, end;
    try
    {
      if ( expression == arg1)
      {
        model.revert();
        string = arg0.evaluateString( context);
        end = (arg2 != null)? end = (int)arg2.evaluateNumber( context): -1;
        String oldResult = substring( string, (int)oldValue, end);

        model.restore();
        string = arg0.evaluateString( context);
        end = (arg2 != null)? end = (int)arg2.evaluateNumber( context): -1;
        String newResult = substring( string, (int)newValue, end);
        
        if ( !newResult.equals( oldResult)) parent.notifyChange( this, context, newResult, oldResult);
      }
      else
      {
        model.revert();
        string = arg0.evaluateString( context);
        start = (int)arg1.evaluateNumber( context);
        String oldResult = substring( string, start, (int)oldValue);
        
        model.restore();
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
