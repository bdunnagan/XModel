/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * IContext.java
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

import java.util.List;
import java.util.concurrent.Executor;

import org.xmodel.IModelObject;
import org.xmodel.Update;
import org.xmodel.xpath.variable.IVariableScope;

/**
 * An interface for the context of an X-Path expression.  The context includes:
 * <ul>
 * <li>context node
 * <li>context position
 * <li>context size
 * </ul>
 * <p>
 * The official X-Path specification includes several other parameters in the
 * context, but they have been omitted here because they were unnecessary.
 * <p>
 * Note that the context position begins at 1.
 */
public interface IContext
{
  /**
   * Returns the parent of this context or null.
   * @return Returns the parent of this context or null.
   */
  public IContext getParent();
  
  /**
   * Returns the root of this context (which may be the context, itself).
   * @return Returns the root of this context.
   */
  public IContext getRoot();

  /**
   * @return Returns the ordered list of context nodes.
   */
  public List<IModelObject> getNodes();
  
  /**
   * Set the specified variable. This method has no effect if the context does not have
   * a variable scope defined. Use the <code>getScope</code> method to verify that a 
   * scope is defined if you need to ensure that the assignment will occur.
   * @param name The name of the variable.
   * @param value The value of the variable.
   * @return Returns the previous value.
   */
  public String set( String name, String value);
  
  /**
   * Set the specified variable. This method has no effect if the context does not have
   * a variable scope defined. Use the <code>getScope</code> method to verify that a 
   * scope is defined if you need to ensure that the assignment will occur.
   * @param name The name of the variable.
   * @param value The value of the variable.
   * @return Returns the previous value.
   */
  public Number set( String name, Number value);
  
  /**
   * Set the specified variable. This method has no effect if the context does not have
   * a variable scope defined. Use the <code>getScope</code> method to verify that a 
   * scope is defined if you need to ensure that the assignment will occur.
   * @param name The name of the variable.
   * @param value The value of the variable.
   * @return Returns the previous value.
   */
  public Boolean set( String name, Boolean value);

  /**
   * Set the specified variable. This method has no effect if the context does not have
   * a variable scope defined. Use the <code>getScope</code> method to verify that a 
   * scope is defined if you need to ensure that the assignment will occur.
   * @param name The name of the variable.
   * @param value The value of the variable.
   * @return Returns the previous value.
   */
  public List<IModelObject> set( String name, List<IModelObject> value);
  
  /**
   * Set the specified variable. This method has no effect if the context does not have
   * a variable scope defined. Use the <code>getScope</code> method to verify that a 
   * scope is defined if you need to ensure that the assignment will occur.
   * @param name The name of the variable.
   * @param value The value of the variable.
   * @return Returns the previous value.
   */
  public List<IModelObject> set( String name, IModelObject value);

  /**
   * Returns the value of the specified variable.
   * @param name The name of the variable.
   * @return Returns null or the value.
   */
  public Object get( String name);

  /**
   * Associate an executor with this context.
   * @param executor The executor.
   */
  public void setExecutor( Executor executor);
  
  /**
   * Returns the executor associated with this context, or if no executor is associated, that of the parent.
   * If no executor is associated in the ancestry of this context, then the default executor defined in
   * GlobalSettings is used.
   * @return Returns the first executor defined in the ancestry of this context.
   */
  public Executor getExecutor();
  
  /**
   * Called by the specified expression when the context is bound.
   * @param expression The expression to which the context was bound.
   */
  public void notifyBind( IExpression expression);
  
  /**
   * Called by the specified expression when the context is unbound.
   * @param expression The expression to which the context was bound.
   */
  public void notifyUnbind( IExpression expression);

  /**
   * Called by the specified expression when notification has been performed for this context.
   * @param expression The expression which performed the notification.
   */
  public void notifyUpdate( IExpression expression);
  
  /**
   * Mark this context as requiring an update for the specified expression. This method is called
   * when the specified expression is bound or unbound and notification is requested.  Marking the
   * context in this way ensures that notification will be performed when the end-user 
   * IExpressionListener is called.
   * @param expression The expression.
   */
  public void markUpdate( IExpression expression);
  
  /**
   * Returns true if the specified expression has performed notification for this context.
   * @param expression The expression to which this context was or will be bound.
   * @return Returns true if the specified expression has performed notification for this context.
   */
  public boolean shouldUpdate( IExpression expression);
  
  /**
   * Returns the last update for which the specified expression notified with this context.
   * @param expression The expression.
   * @return Returns the last update.
   */
  public Update getLastUpdate( IExpression expression);
  
  /**
   * Returns the scope associated with this context.
   * @return Returns the scope associated with this context.
   */
  public IVariableScope getScope();
}
