/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ExpressionException.java
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
package org.xmodel.xpath.expression;

/**
 * An exceptional condition which occurs during the processing of Expressions.
 */
@SuppressWarnings("serial")
public class ExpressionException extends RuntimeException
{
  /**
   * Create an ExpressionException with the given message.
   * @param expression The expression.
   * @param message The exception message.
   */
  public ExpressionException( IExpression expression, String message)
  {
    super( createMessage( expression, message));
  }

  /**
   * Create an ExpressionException with the given message and cause.
   * @param expression The expression.
   * @param message The exception message.
   * @param cause The causing exception.
   */
  public ExpressionException( IExpression expression, String message, Throwable cause)
  {
    super( createMessage( expression, message), cause);
  }
  
  /**
   * Create the exception message.
   * @param expression The expression.
   * @param message The exception message.
   * @return Returns the exception message.
   */
  static protected String createMessage( IExpression expression, String message)
  {
    StringBuilder builder = new StringBuilder();
    builder.append( message); builder.append( '\n');
    builder.append( createExpressionSummary( expression)); builder.append( '\n');
    return builder.toString();
  }
  
  /**
   * Returns a string containing the complete expression with a pointer indicating the part of
   * the expression where the error occurred.
   * @param expression The expression.
   * @return Returns the expression summary.
   */
  static protected String createExpressionSummary( IExpression expression)
  {
    return expression.toString();
  }
}
