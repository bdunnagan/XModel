/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xpath.expression;

import java.util.List;

import dunnagan.bob.xmodel.IModelObject;

/**
 * An interface for receiving notification about changes to the result of an IExpression.
 */
public interface IExpressionListener
{
  /**
   * Called when one or more nodes are added to the bound expression's node-set.
   * @param expression The expression whose node-set has changed.
   * @param context The context of the expression evaluation.
   * @param nodes The nodes which were added.
   */
  public void notifyAdd( IExpression expression, IContext context, List<IModelObject> nodes);

  /**
   * Called when one or more nodes are removed from the bound expression's node-set.
   * @param expression The expression whose node-set has changed.
   * @param context The context of the expression evaluation.
   * @param nodes The nodes which were removed.
   */
  public void notifyRemove( IExpression expression, IContext context, List<IModelObject> nodes);

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
   * Called when the result of an expression has changed. The client must reevaluate the entire
   * expression in order to determine what has changed.
   * @param expression The expression.
   * @param context The context.
   */
  public void notifyChange( IExpression expression, IContext context);
  
  /**
   * Returns true if this expression requires value notification.
   * @return Returns true if this expression requires value notification.
   */
  public boolean requiresValueNotification();
  
  /**
   * Called when the value of a node in the current node-set changes.
   * @param expression The expression.
   * @param contexts The contexts in which the expression was evaluated which yielded the object.
   * @param object The object whose value changed.
   * @param newValue The new value.
   * @param oldValue The old value.
   */
  public void notifyValue( IExpression expression, IContext[] contexts, IModelObject object, Object newValue, Object oldValue);
  
  /**
   * Handle an exception encountered during partial evaluation.
   * @param expression The expression which needs to be reevaluated.
   * @param context The context of the expression evaluation.
   * @param e The exception.
   */
  public void handleException( IExpression expression, IContext context, Exception e);
}
