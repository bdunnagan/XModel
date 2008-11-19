/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.xpath.variable;

import java.util.Collection;
import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.IModelObjectFactory;
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
   * Set the specified variable with an automatically generated element whose values it the POJO (plain, old Java object).
   * The generated element has a reserved element name in the XModel namespace which indicates its POJO content.
   * @param name The name of the variable.
   * @param pojo The plain, old Java object.
   * @param factory An optional factory for creating the generated element.
   * @return Returns the previous content of the variable.
   */
  public Object setPojo( String name, Object pojo, IModelObjectFactory factory);
  
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
   * Returns the POJO (plain, old Java object) stored in the value of the element assigned to the variable.
   * Only the first element in the variable node-set is considered.  Returns null if variable does not 
   * contain a node-set.
   * @param name The name of the variable.
   * @return Returns the POJO stored in the element assigned to the variable.
   */
  public Object getPojo( String name);
  
  /**
   * Get the value of the specified variable and evaluate its expression if necessary.
   * @param name The name of the variable.
   * @param context The context of the variable evaluation (not used for literals).
   * @return Returns the value of the specified variable.
   */
  public Object get( String name, IContext context) throws ExpressionException;

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