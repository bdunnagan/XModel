/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * IDebugger.java
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
package org.xmodel.xaction.debug;

import org.xmodel.xaction.IXAction;
import org.xmodel.xpath.expression.IContext;

/**
 * An interface for debugging XAction flows.
 */
public interface IDebugger
{
  /**
   * Push the specified action on the stack.
   * @param context The context.
   * @param action The action.
   */
  public void push( IContext context, IXAction action);

  /**
   * Pop the current action off the stack.
   */
  public void pop();
}