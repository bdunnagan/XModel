/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel;

import java.util.List;

import dunnagan.bob.xmodel.xpath.expression.IContext;
import dunnagan.bob.xmodel.xpath.expression.IExpressionListener;

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
   * Returns true if the specified object meets the constraint criteria. The position and size
   * of the node-set to which the object belong are assumed to be unknown. Therefore, this method
   * may result in the path being partially reevaluated.
   * @param parent The parent context or null.
   * @param candidatePath The candidate path (see ModelAlgorithms).
   * @param candidate The candidate object to be tested.
   * @return Returns true if the object meets the constraint criteria.
   */
  public boolean evaluate( IContext parent, IPath candidatePath, IModelObject candidate);

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
   * Bind the specified context to the predicate.
   * @param context The context.
   */
  public void bind( IContext context);
  
  /**
   * Unbind the specified context from the predicate.
   * @param context The context.
   */
  public void unbind( IContext context);
  
  /**
   * Returns a clone of this predicate.
   * @return Returns a clone of this predicate.
   */
  public Object clone();
}
