/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * IFunctionFactory.java
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
