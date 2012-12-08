/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * IPredicate.java
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
package org.xmodel;

import java.util.List;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpressionListener;


/**
 * An interface for filtering the nodes returned by an IPath. IPredicate instances are associated with an
 * IPathElement and constrain the nodes which are returned by the IPathElement. The interface supports 
 * variable bindings. However, the interpretation of variable bindings is implementation-specific.
 */
public interface IPredicate
{
  /**
   * Set the path to which this predicate belongs.
   * @param path The path.
   */
  public void setParentPath( IPath path);
  
  /**
   * Filter the specified nodes using this predicate.
   * @param parent The parent context.
   * @param nodes The node-set to be filtered.
   */
  public void filter( IContext parent, List<IModelObject> nodes);

  /**
   * Filter the specified nodes using this predicate.  The node-set passed to this method may not
   * be the complete node-set being evaluated against the predicate.  In many cases, the complete
   * node-set is not needed.  However, the candidatePath argument will return the complete node-set
   * if required.
   * @param parent The parent context.
   * @param candidatePath A path that will return the complete context node-set, if required.
   * @param pathLength The length of the candidatePath to evaluate.
   * @param nodes The node-set to be filtered.
   */
  public void filter( IContext parent, IPath candidatePath, int pathLength, List<IModelObject> nodes);

  /**
   * Returns true if this predicate contains an expression which is absolute.
   * @param context Null or the context to be tested.
   * @return Returns true if the predicate is absolute.
   */
  public boolean isAbsolute( IContext context);

  /**
   * Add a listener on the predicate expression.
   * @param listener The listener.
   */
  public void addListener( IExpressionListener listener);

  /**
   * Remove a listener from the predicate expression.
   * @param listener The listener.
   */
  public void removeListener( IExpressionListener listener);
  
  /**
   * Returns the listeners installed on this predicate.
   * @return Returns the listeners installed on this predicate.
   */
  public List<IExpressionListener> getPredicateListeners();
  
  /**
   * Bind the specified nodes with this predicate. 
   * @param parent The parent context.
   * @param nodes The node-set to be bound.
   */
  public void bind( IContext parent, List<IModelObject> nodes);
  
  /**
   * Bind the specified nodes with this predicate.  The node-set passed to this method may not
   * be the complete node-set being evaluated against the predicate.  In many cases, the complete
   * node-set is not needed.  However, the candidatePath argument will return the complete node-set
   * if required.
   * @param parent The parent context.
   * @param candidatePath A path that will return the complete context node-set, if required.
   * @param pathLength The length of the path to evaluate.
   * @param nodes The node-set to be filtered.
   */
  public void bind( IContext parent, IPath candidatePath, int pathLength, List<IModelObject> nodes);
  
  /**
   * Unbind the specified nodes with this predicate. 
   * @param parent The parent context.
   * @param nodes The node-set to be bound.
   */
  public void unbind( IContext parent, List<IModelObject> nodes);
  
  /**
   * Unbind the specified nodes with this predicate.  The node-set passed to this method may not
   * be the complete node-set being evaluated against the predicate.  In many cases, the complete
   * node-set is not needed.  However, the candidatePath argument will return the complete node-set
   * if required.
   * @param parent The parent context.
   * @param candidatePath A path that will return the complete context node-set, if required.
   * @param pathLength The length of the path to evaluate.
   * @param nodes The node-set to be filtered.
   */
  public void unbind( IContext parent, IPath candidatePath, int pathLength, List<IModelObject> nodes);
  
  /**
   * Returns a clone of this predicate.
   * @return Returns a clone of this predicate.
   */
  public Object clone();
}
