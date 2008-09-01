/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.xpath.expression;

/**
 * An exceptional condition which occurs during the processing of Expressions.
 */
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
