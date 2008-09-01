/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.xpath.function;

import org.xmodel.xpath.expression.IExpression;

/**
 * An interface for an IFunction factory.
 */
public interface IFunctionFactory
{
  /**
   * Register the specified client function. Registration is not synchronized and functions
   * are global for all XPath expressions. If the name conflicts with an existing XPath 
   * function name, the previous function is replaced.
   * @param functionName The name of the function.
   * @param clss The function class.
   */
  public void register( String functionName, Class<? extends Function> clss);
  
  /**
   * Register the specified client function using a prototype of the function. Registration is
   * not synchronized and functions are global for all XPath expressions. If the name conflicts
   * with an existing XPath function name, the previous function is replaced.
   * @param functionName The name of the function.
   * @param prototype The prototype instance of the function.
   */
  public void register( String functionName, Function prototype);
  
  /**
   * Create the IFunction object corresponding to the given function name.
   * @param functionName The name of the function to be created.
   * @return Returns an IFunction object with the given function name.
   */
  public IExpression createFunction( String functionName);
}
