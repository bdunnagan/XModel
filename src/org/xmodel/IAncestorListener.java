/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * IAncestorListener.java
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

/**
 * An interface for notifications about changes in the structure of a domain model tree. 
 */
public interface IAncestorListener
{
  /**
   * This method is called when the specified ancestor of the specified object
   * is connected to the parent object.
   * @param object The decendant of the ancestor.
   * @param ancestor The ancestor of the specified object.
   * @param newParent The new parent of the ancestor.
   * @param oldParent The old parent of the ancestor.
   */
  public void notifyAttach( INode object, INode ancestor, INode newParent, INode oldParent);

  /**
   * This method is called when the specified ancestor of the specified object
   * is disconnected from the parent object.
   * @param object The decendant of the ancestor.
   * @param ancestor The ancestor of the specified object.
   * @param oldParent The old parent of the ancestor.
   */
  public void notifyDetach( INode object, INode ancestor, INode oldParent);
}
