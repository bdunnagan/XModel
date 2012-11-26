/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * IExpression.java
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
import org.xmodel.IChangeSet;
import org.xmodel.INode;
import org.xmodel.INodeFactory;
import org.xmodel.xpath.variable.IVariableSource;


/**
 * An interface for XPath expressions.  Each expression implementation has and name and arguments. 
 * The interface provides notification through the IExpressionListener interface.  An IExpressionListener
 * receives notification for a specified context.
 */
public interface IExpression
{
  public enum ResultType { NODES, STRING, NUMBER, BOOLEAN, UNDEFINED};
  
  /**
   * Returns the name of this expression.
   * @return Returns the name of this expression.
   */
  public String getName();

  /**
   * Returns the result type of this expression wihtout a context scope.
   * @return Returns the result type of this expression without a context scope.
   */
  public ResultType getType();

  /**
   * Returns the result type of this expression within a context scope. This is necessary for 
   * variable expression which are resolve to a variable assignment in a context scope.
   * @param context The context of the evaluation.
   * @return Returns the result type of this expression within a context scope.
   */
  public ResultType getType( IContext context);
  
  /**
   * Add an expression argument to the list of arguments.
   * @param expression The expression argument to be added.
   */
  public void addArgument( IExpression argument);
  
  /**
   * Remove an expression argument from the list of arguments.
   * @param expression The expression argument to be added.
   */
  public void removeArgument( IExpression argument);

  /**
   * Returns the list of arguments.
   * @return Returns the list of arguments.
   */
  public List<IExpression> getArguments();

  /**
   * Returns the argument with the specified index.
   * @param index The index of the argument.
   * @return Returns the argument with the specified index.
   */
  public IExpression getArgument( int index);
  
  /**
   * Set the parent of this expression. This method is for internal use only.
   * @param parent The parent.
   */
  public void internal_setParent( IExpression parent);
  
  /**
   * Returns the parent expression or null.
   * @return Returns the parent expression or null.
   */
  public IExpression getParent();
  
  /**
   * Returns the root expression.
   * @return Returns the root expression.
   */
  public IExpression getRoot();
  
  /**
   * Returns true if the specified expression is an ancestor of this expression.
   * @param ancestor The ancestor to test.
   * @return Returns true if the specified expression is an ancestor of this expression.
   */
  public boolean isAncestor( IExpression ancestor);
  
  /**
   * Returns true if this expression is absolute. An absolute expression is an expression which 
   * will return the same result when evaluated with any context in a particular model.
   * @param context Null or the context to be tested.
   * @return Returns true if this expression is absolute.
   */
  public boolean isAbsolute( IContext context);
  
  /**
   * Set a local variable.
   * @param name The name of the variable.
   * @param value The value of the variable.
   */
  public void setVariable( String name, String value);
  
  /**
   * Set a local variable.
   * @param name The name of the variable.
   * @param value The value of the variable.
   */
  public void setVariable( String name, Number value);
  
  /**
   * Set a local variable.
   * @param name The name of the variable.
   * @param value The value of the variable.
   */
  public void setVariable( String name, Boolean value);
  
  /**
   * Set a local variable.
   * @param name The name of the variable.
   * @param node A node.
   */
  public void setVariable( String name, INode node);
  
  /**
   * Set a local variable.
   * @param name The name of the variable.
   * @param nodes A list of nodes.
   */
  public void setVariable( String name, List<INode> nodes);
  
  /**
   * Define a local variable with another expression.
   * @param name The name of the variable.
   * @param expression The expression.
   */
  public void setVariable( String name, IExpression expression);
  
  /**
   * Returns the IVariableSource for this expression.
   * @return Returns the IVariableSource for this expression.
   */
  public IVariableSource getVariableSource();
  
  /**
   * Returns true if this expression tree requires the position and size arguments of the context.
   * @return Returns true if this expression tree requires the position and size arguments of the context.
   */
  public boolean requiresOrdinalContext();
  
  /**
   * Returns the node-set result of this expression for the specified context.
   * @param context The context in which to evaluate the expression.
   * @return Returns the node-set result of the expression.
   */
  public List<INode> evaluateNodes() throws ExpressionException;

  /**
   * Returns the string result of this expression for the specified context.
   * @param context The context in which to evaluate the expression.
   * @return Returns the string result of the expression.
   */
  public String evaluateString() throws ExpressionException;
  
  /**
   * Returns the numeric result of this expression for the specified context.
   * @param context The context in which to evaluate the expression.
   * @return Returns the numeric result of the expression.
   */
  public double evaluateNumber() throws ExpressionException;
  
  /**
   * Returns the boolean result of this expression for the specified context.
   * @param context The context in which to evaluate the expression.
   * @return Returns the boolean result of the expression.
   */
  public boolean evaluateBoolean() throws ExpressionException;
  
  /**
   * Returns the node-set result of this expression for the specified context.
   * @param context The context in which to evaluate the expression.
   * @return Returns the node-set result of the expression.
   */
  public List<INode> evaluateNodes( IContext context) throws ExpressionException;

  /**
   * Returns the string result of this expression for the specified context.
   * @param context The context in which to evaluate the expression.
   * @return Returns the string result of the expression.
   */
  public String evaluateString( IContext context) throws ExpressionException;
  
  /**
   * Returns the numeric result of this expression for the specified context.
   * @param context The context in which to evaluate the expression.
   * @return Returns the numeric result of the expression.
   */
  public double evaluateNumber( IContext context) throws ExpressionException;
  
  /**
   * Returns the boolean result of this expression for the specified context.
   * @param context The context in which to evaluate the expression.
   * @return Returns the boolean result of the expression.
   */
  public boolean evaluateBoolean( IContext context) throws ExpressionException;
  
  /**
   * Returns the node-set result of this expression for the specified context. If an ExpressionException
   * is caught then the default return value is returned.
   * @param context The context in which to evaluate the expression.
   * @param defaultResult The return value if an ExpressionException is caught.
   * @return Returns the node-set result of the expression.
   */
  public List<INode> evaluateNodes( IContext context, List<INode> defaultResult);

  /**
   * Returns the string result of this expression for the specified context. If an ExpressionException
   * is caught then the default return value is returned.
   * @param context The context in which to evaluate the expression.
   * @param defaultResult The return value if an ExpressionException is caught.
   * @return Returns the string result of the expression.
   */
  public String evaluateString( IContext context, String defaultResult);
  
  /**
   * Returns the numeric result of this expression for the specified context. If an ExpressionException
   * is caught then the default return value is returned.
   * @param context The context in which to evaluate the expression.
   * @param defaultResult The return value if an ExpressionException is caught.
   * @return Returns the numeric result of the expression.
   */
  public double evaluateNumber( IContext context, double defaultResult);
  
  /**
   * Returns the boolean result of this expression for the specified context. If an ExpressionException
   * is caught then the default return value is returned.
   * @param context The context in which to evaluate the expression.
   * @param defaultResult The return value if an ExpressionException is caught.
   * @return Returns the boolean result of the expression.
   */
  public boolean evaluateBoolean( IContext context, boolean defaultResult);

  /**
   * (Optional) Create a subtree relative to the specified context which completes this expression.
   * @param context The context where the subtree will be created.
   * @param factory Null or the factory for creating the subtree elements.
   * @param undo Null or a change set containing records which will undo the creation.
   */
  public void createSubtree( IContext context, INodeFactory factory, IChangeSet undo);
  
  /**
   * This method is provided as a convenience for performing simple path queries using this expression.
   * If the expression does not return a node-set, then this method will return an empty list.
   * @param object The context object.
   * @param result An optional location to store the result.
   * @return Returns a new list or the result argument if it is non-null.
   */
  public List<INode> query( INode object, List<INode> result);

  /**
   * This method is provided as a convenience for performing simple path queries using this expression.
   * If the expression does not return a node-set, then this method will return null.
   * @param object The context object.
   * @return Returns null or the first node in the node-set.
   */
  public INode queryFirst( INode object);
  
  /**
   * Evaluate the expression with an empty context (only expressions that begin with
   * the collection function will return nodes).
   * @param result Null or the list to populate.
   * @return Returns the argument or a new list if the argument is null.
   */  
  public List<INode> query( List<INode> result);
  
  /**
   * Evaluate the expression with an empty context (only expressions that begin with
   * the collection function will return nodes) and return the first node.
   * @return Returns the first node in the node-set.
   */
  public INode queryFirst();
  
  /**
   * This method is provided as a convenience for performing simple path queries using this expression.
   * If the expression does not return a node-set, then this method will return an empty list.
   * @param context The context.
   * @param result An optional location to store the result.
   * @return Returns a new list or the result argument if it is non-null.
   */
  public List<INode> query( IContext context, List<INode> result);

  /**
   * This method is provided as a convenience for performing simple path queries using this expression.
   * If the expression does not return a node-set, then this method will return null.
   * @param context The context.
   * @return Returns null or the first node in the node-set.
   */
  public INode queryFirst( IContext context);
  
  /**
   * Add an IExpressionListener to this expression for the specified context.
   * (This method is only implemented by the root expression.)
   * @param context The context in which to evaluate the expression.
   * @param listener The listener which will be notified of changes to the expression result.
   */
  public void addListener( IContext context, IExpressionListener listener);
  
  /**
   * Remove an IExpressionListener from this expression for the specified context.
   * (This method is only implemented by the root expression.)
   * @param context The context in which the expression was evaluated.
   * @param listener The listener which was previously registered.
   */
  public void removeListener( IContext context, IExpressionListener listener);
  
  /**
   * Add an IExpressionListener to this expression for the specified context, evaluate the
   * expression in the context and notify the listener of the initial evaluation result.
   * Implementations should call the <code>addListener</code> method so that subclasses which need
   * to override the <code>addListener</code> method don't need to override this method too. 
   * (This method is only implemented by the root expression.)
   * @param context The context in which to evaluate the expression.
   * @param listener The listener which will be notified of changes to the expression result.
   */
  public void addNotifyListener( IContext context, IExpressionListener listener);

  /**
   * Remove an IExpressionListener from this expression for the specified context, evaluate the expression
   * in the context and notify the listener of the final evaluation result. Implementations should
   * call the <code>removeListener</code> method so that subclasses which need to override the 
   * <code>removeListener</code> method don't need to override this method too.
   * (This method is only implemented by the root expression.)
   * @param context The context in which the expression was evaluated.
   * @param listener The listener which was previously registered.
   */
  public void removeNotifyListener( IContext context, IExpressionListener listener);
  
  /**
   * Returns the listeners on this IExpression.
   * (This method is only implemented by the root expression.)
   * @return Returns the listeners on this IExpression.
   */
  public ExpressionListenerList getListeners();
  
  /**
   * Install listeners to detect updates to the evaluation result of this expression.
   * The specified context will be the context scope for the duration of the bind.
   * @param context The context.
   */
  public void bind( IContext context);
  
  /**
   * Remove listeners that detect updates to the evaluation result of this expression.
   * The specified context will be the context scope for the duration of the unbind.
   * @param context The context.
   */
  public void unbind( IContext context);

  /**
   * Called when one or more nodes are added to the bound expression's node-set.
   * @param expression The expression whose node-set has changed.
   * @param context The context of the expression evaluation.
   * @param nodes The nodes which were added.
   */
  public void notifyAdd( IExpression expression, IContext context, List<INode> nodes);

  /**
   * Called when one or more nodes are removed from the bound expression's node-set.
   * @param expression The expression whose node-set has changed.
   * @param context The context of the expression evaluation.
   * @param nodes The nodes which were removed.
   */
  public void notifyRemove( IExpression expression, IContext context, List<INode> nodes);

  /**
   * Called when the result of an expression which evaluates to a <i>STRING</i> changes.
   * @param expression The expression which needs to be reevaluated.
   * @param context The context of the expression evaluation.
   * @param newValue The new value.
   * @param oldValue The old value.
   */
  public void notifyChange( IExpression expression, IContext context, String newValue, String oldValue);

  /**
   * Called when the result of an expression which evaluates to a <i>NUMBER</i> changes.
   * @param expression The expression which needs to be reevaluated.
   * @param context The context of the expression evaluation.
   * @param newValue The new value.
   * @param oldValue The old value.
   */
  public void notifyChange( IExpression expression, IContext context, double newValue, double oldValue);

  /**
   * Called when the result of an expression which evaluates to a <i>BOOLEAN</i> changes.
   * @param expression The expression which needs to be reevaluated.
   * @param context The context of the expression evaluation.
   * @param newValue The new value.
   */
  public void notifyChange( IExpression expression, IContext context, boolean newValue);
  
  /**
   * Called when the result of an expression changes in an unknown way (or not at all).
   * @param expression The expression whose value has changed.
   * @param context The context which is affected.
   */
  public void notifyChange( IExpression expression, IContext context);
  
  /**
   * Called from LeafValueListener when it detects that the value of a node has changed.
   * @param expression The common ancestor of all expression registered for value notification.
   * @param contexts The contexts in which the expression was evaluated which yielded the object.
   * @param object The object whose value changed.
   * @param newValue The new value.
   * @param oldValue The old value.
   */
  public void notifyValue( IExpression expression, IContext[] contexts, INode object, Object newValue, Object oldValue);
  
  /**
   * Handle an exception encountered during partial evaluation.
   * @param expression The expression which needs to be reevaluated.
   * @param context The context of the expression evaluation.
   * @param e The exception.
   */
  public void handleException( IExpression expression, IContext context, Exception e);

  /**
   * Returns true if this expression requires value notification from the specified argument
   * expression which returns a node-set. It is the responsibility of argument expressions to
   * call this method on their parent and to install a LeafValueListener if the result is true.
   * @param argument The argument expression.
   * @return Returns true if this expression requires value notification from the argument.
   */
  public boolean requiresValueNotification( IExpression argument);
  
  /**
   * Returns a deep copy of the expression tree.
   * @return Returns a deep copy of the expression tree.
   */
  public Object clone();
}
