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

import java.util.List;

import org.xmodel.xaction.IXAction;
import org.xmodel.xpath.expression.IContext;

/**
 * An interface for debugging XAction flows.
 */
public interface IDebugger
{
  /**
   * Execute the next action and pause.
   * @return Returns the result of the action.
   */
  public Object[] step();
  
  /**
   * Execute the specified actions belonging to the same stack frame.
   * @param context The context.
   * @param script The script containing the actions.
   * @param actions The actions to be executed.
   * @return Returns the result like ScriptAction.
   */
  public Object[] run( IContext context, IXAction script, List<IXAction> actions);
}