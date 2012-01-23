/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * IModelRegistry.java
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
 * An interface for adding, removing and accessing documents of collections and for accessing the
 * IModel associated with the current thread.  Each thread which manages an xmodel has its own
 * instance of IModel.
 */
public interface IModelRegistry
{
  /**
   * Returns the IModel associated with the current thread.
   * @return Returns the IModel associated with the current thread.
   */
  public IModel getModel();
  
  /**
   * Create a collection with the specified name and root element.
   * @param name The name of the collection and the root element.
   * @return Returns the root of the collection.
   */
  public IModelObject createCollection( String name);
}
