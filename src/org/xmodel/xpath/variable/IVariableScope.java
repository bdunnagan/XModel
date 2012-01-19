/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * IVariableScope.java
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

import java.util.Collection;
import java.util.List;

import org.xmodel.IModelObject;
import org.xmodel.memento.IMemento;
import org.xmodel.xpath.expression.ExpressionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.IExpression.ResultType;


/**
 * An interface which defines a scope of storage for variables.
 */
public interface IVariableScope
{
  /**
   * Called when the scope is added to an IVariableSource.
   * @param source The source.
   */
  public void internal_setSource( IVariableSource source);
  
  /**
   * Returns the IVariableSource to which this scope belongs.
   * @return Returns the IVariableSource to which this scope belongs.
   */
  public IVariableSource getSource();
  
  /**
   * Returns the precedence (priority) of the scope. Higher values have greater precedence.
   * @return Returns the precedence (priority) of the scope.
   */
  public int getPrecedence();
  
  /**
   * Returns the name of the scope (e.g. global).
   * @return Returns the name of the scope.
   */
  public String getName();
  
  /**
   * @return Returns the names of all variables defined in this scope.
   */
  public Collection<String> getVariables();

  /**
   * Set the value of a variable.
   * @param name The name of the variable.
   * @param value The new value.
   * @return Returns the old value.
   */
  public Object set( String name, Object value);

  /**
   * Insert an object into the sequence stored in the specified variable.
   * @param name The name of the variable.
   * @param object The object to insert.
   */
  public void insert( String name, Object object);
  
  /**
   * Insert an object into the sequence stored in the specified variable.
   * @param name The name of the variable.
   * @param object The object to insert.
   * @param index The index of insertion.
   */
  public void insert( String name, Object object, int index);

  /**
   * Remove an object from the sequence stored in the specified variable.
   * @param name The name of the variable.
   * @param object The object to remove.
   */
  public void remove( String name, Object object);
  
  /**
   * Remove an object from the sequence stored in the specified variable.
   * @param name The name of the variable.
   * @param index The index of the object to remove.
   */
  public void remove( String name, int index);
  
  /**
   * Set the specified variable.
   * @param name The name of the variable.
   * @param value The value of the variable.
   * @return Returns the previous value.
   */
  public String set( String name, String value);
  
  /**
   * Set the specified variable.
   * @param name The name of the variable.
   * @param value The value of the variable.
   * @return Returns the previous value.
   */
  public Number set( String name, Number value);
  
  /**
   * Set the specified variable.
   * @param name The name of the variable.
   * @param value The value of the variable.
   * @return Returns the previous value.
   */
  public Boolean set( String name, Boolean value);
  
  /**
   * Set the specified variable.
   * @param name The name of the variable.
   * @param value The value of the variable.
   * @return Returns the previous value.
   */
  public List<IModelObject> set( String name, List<IModelObject> value);
  
  /**
   * Set the specified variable.
   * @param name The name of the variable.
   * @param value The value of the variable.
   * @return Returns the previous value.
   */
  public List<IModelObject> set( String name, IModelObject value);

  /**
   * Define the specified variable with the specified expression. This method may only be called
   * once for a given variable. The definition is permanent.
   * @param name The name of the variable.
   * @param expression The expression.
   */
  public void define( String name, IExpression expression);
  
  /**
   * Get the raw value of the specified variable without evaluating expressions.
   * @param name The name of the variable.
   * @param context The context of the variable evaluation (not used for literals).
   * @return Returns the value of the specified variable.
   */
  public Object get( String name);

  /**
   * Get the value of the specified variable and evaluate its expression if necessary.
   * @param name The name of the variable.
   * @param context The context of the variable evaluation (not used for literals).
   * @return Returns the value of the specified variable.
   */
  public Object get( String name, IContext context) throws ExpressionException;

  /**
   * Clear the specified variable so that it appears undefined.  This operation does not perform any notification.
   * @param name The name of the variable.
   */
  public void clear( String name);
  
  /**
   * Returns the names of all variables defined in this scope.
   * @return Returns the names of all variables defined in this scope.
   */
  public Collection<String> getAll();
  
  /**
   * Copy the variables from the specified scope.
   * @param scope The scope.
   */
  public void copyFrom( IVariableScope scope);
  
  /**
   * Returns true if the specified variable is defined.
   * @param name The name of the variable.
   * @return Returns true if the specified variable is defined.
   */
  public boolean isDefined( String name);
  
  /**
   * Returns true if there is a listener registered on the specified variable.
   * @param name The name of the variable.
   * @return Returns true if there is a listener registered on the specified variable.
   */
  public boolean isBound( String name);
  
  /**
   * Returns the type of the specified variable.
   * @param name The name of the variable.
   * @return Returns the type of the specified variable.
   */
  public ResultType getType( String name);
  
  /**
   * Returns the type of the specified variable in the specified context. This method should be called
   * whenever the context is known. If the variable is defined with an expression then the context is
   * used to get the type of the expression in that context.
   * @param name The name of the variable.
   * @param context The evaluation context.
   * @return Returns the type of the specified variable in the specified context.
   */
  public ResultType getType( String name, IContext context);
  
  /**
   * Register for notification of variable updates.
   * @param name The name of the variable.
   * @param context The context in which the variable is evaluated.
   * @param listener The listener.
   */
  public void addListener( String name, IContext context, IVariableListener listener);
  
  /**
   * Remove a listener.
   * @param name The name of the variable.
   * @param context The context in which the variable is evaluated.
   * @param listener The listener.
   */
  public void removeListener( String name, IContext context, IVariableListener listener);
  
  /**
   * Revert the specified VariableMemento.
   * @param memento The memento.
   */
  public void revert( IMemento memento);

  /**
   * Restore the specified VariableMemento.
   * @param memento The memento.
   */
  public void restore( IMemento memento);
  
  /**
   * Returns a clone of this variable scope including variable assignments.
   * @return Returns a clone of this variable scope.
   */
  public IVariableScope cloneOne();
}