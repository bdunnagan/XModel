/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * IMemento.java
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
package org.xmodel.memento;

/**
 * An interface for a memento of a discreet xmodel update. There a five implementations of this
 * interface for the five types of updates:
 * <ul>
 * <li>SetAttributeMemento
 * <li>RemoveAttributeMemento
 * <li>AddChildMemento
 * <li>RemoveChildMemento
 * <li>VariableMemento
 * </ul>
 * When the <code>revert</code> method is called, the effect of the update is undone without calling
 * any listeners. When the <code>restore</code> method is called, the effect of the update is restored
 * without calling listeners.
 */
public interface IMemento
{
  /**
   * Silently revert this update.
   */
  public void revert();

  /**
   * Silently restore this update.
   */
  public void restore();
  
  /**
   * Clear the data for this memento.
   */
  public void clear();
}
