/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * IVariableSource.java
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
package org.xmodel.xpath.variable;

import java.util.List;
import org.xmodel.xpath.expression.ExpressionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression.ResultType;


/**
 * An interface for supplying variable assignments to a VariableExpression. Each VariableExpression
 * is associated with exactly one IVariableSource. Every VariableExpression in an expression tree 
 * uses the same IVariableSource. An IVariableSource at least one IVariableScope instance which is
 * the repository of the variable assignment. The IVariableScope instance with the highest precedence
 * resolves a variable assignment. If a variable is defined in one scope and subsequently redefined
 * in a scope with higher precedence, the variable scope will change and VariableExpressions will be
 * updated through the listener interface.
 */
public interface IVariableSource
{
  /**
   * Set the parent.
   * @param parent The parent.
   */
  public void setParent( IVariableSource parent);

  /**
   * Return the parent.
   * @return Returns the parent.
   */
  public IVariableSource getParent();
  
  /**
   * Add the specified scope. Adding a scope does not trigger notification.
   * For convenience, a null argument must be ignored.
   * @param scope The scope.
   */
  public void addScope( IVariableScope scope);
  
  /**
   * Remove the specified scope. Removing a scope does not trigger notification.
   * For convenience, a null argument must be ignored.
   * @param scope The scope.
   */
  public void removeScope( IVariableScope scope);
  
  /**
   * Returns all the scopes defined on this source.
   * @return Returns all the scopes defined on this source.
   */
  public List<IVariableScope> getScopes();
  
  /**
   * Returns the specified scope.
   * @param scopeName The name of the scope.
   * @return Returns the specified scope.
   */
  public IVariableScope getScope( String scopeName);

  /**
   * Returns the scope which defines the specified variable.
   * @param variable The name of the variable.
   * @return Returns the scope which defines the specified variable.
   */
  public IVariableScope getVariableScope( String variable);

  /**
   * Returns the type of the specified variable in the visible scope.
   * @param variable The name of the variable.
   * @return Returns the type of the specified variable in the visible scope.
   */
  public ResultType getVariableType( String variable);
  
  /**
   * Returns the type of the specified variable in the visible scope in the specified context. If the
   * variable is defined with an expression then the type of the variable may be determined by the 
   * context in which the associated expression is evaluated.
   * @param variable The name of the variable.
   * @return Returns the type of the specified variable in the visible scope in the specified context.
   */
  public ResultType getVariableType( String variable, IContext context);
  
  /**
   * Returns the value of the variable in the given context.
   * @param variable The name of the variable.
   * @param context The context of the evaluation.
   * @return Returns the value of the variable in the given context.
   */
  public Object getVariable( String variable, IContext context) throws ExpressionException;
}
