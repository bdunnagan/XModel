/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * IVariableListener.java
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
import org.xmodel.INode;
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
  public void notifyAdd( String name, IVariableScope scope, IContext context, List<INode> nodes);

  /**
   * Called when the node-set of a variable changes.
   * @param name The name of the variable.
   * @param scope The scope of the variable.
   * @param context The context of the variable evaluation.
   * @param nodes The nodes which were removed.
   */
  public void notifyRemove( String name, IVariableScope scope, IContext context, List<INode> nodes);

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
