/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.xpath.variable;

import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.xpath.expression.IContext;


/**
 * An interface for receiving notification of variable updates.
 */
public interface IVariableListener
{
  /**
   * Called when the node-set of a variable changes.
   * @param name The name of the variable.
   * @param scope The scope of the variable.
   * @param context The context of the variable evaluation.
   * @param nodes The nodes which were added.
   */
  public void notifyAdd( String name, IVariableScope scope, IContext context, List<IModelObject> nodes);

  /**
   * Called when the node-set of a variable changes.
   * @param name The name of the variable.
   * @param scope The scope of the variable.
   * @param context The context of the variable evaluation.
   * @param nodes The nodes which were removed.
   */
  public void notifyRemove( String name, IVariableScope scope, IContext context, List<IModelObject> nodes);

  /**
   * Called when a string variable changes.
   * @param name The name of the variable.
   * @param scope The scope of the variable.
   * @param context The context of the variable evaluation.
   * @param newValue The new value.
   * @param oldValue The old value.
   */
  public void notifyChange( String name, IVariableScope scope, IContext context, String newValue, String oldValue);

  /**
   * Called when a numeric variable changes.
   * @param name The name of the variable.
   * @param scope The scope of the variable.
   * @param context The context of the variable evaluation.
   * @param newValue The new value.
   * @param oldValue The old value.
   */
  public void notifyChange( String name, IVariableScope scope, IContext context, Number newValue, Number oldValue);

  /**
   * Called when a boolean variable changes.
   * @param name The name of the variable.
   * @param scope The scope of the variable.
   * @param context The context of the variable evaluation.
   * @param newValue The new value.
   */
  public void notifyChange( String name, IVariableScope scope, IContext context, Boolean newValue);
}
