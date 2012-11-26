/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * IPathListener.java
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


/**
 * An interface for notification for the addition and removal of domain objects with a specified
 * model path. The notification happens regardless of how the object is added to or removed from the
 * model. The path may contain elements with any supported axis including ANCESTOR and DECENDENT.
 */
public interface IPathListener
{
  /**
   * Called when one or more domain objects are added to a layer of the specified path.
   * @param context The context.
   * @param path The path.
   * @param pathIndex The layer index.
   * @param nodes The nodes which were added.
   */
  public void notifyAdd( IContext context, IPath path, int pathIndex, List<INode> nodes);

  /**
   * Called when one or more domain objects are removed from a layer of the specified path.
   * @param context The context.
   * @param path The path.
   * @param pathIndex The layer index.
   * @param nodes The nodes which were removed.
   */
  public void notifyRemove( IContext context, IPath path, int pathIndex, List<INode> nodes);
}
