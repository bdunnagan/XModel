/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ICache.java
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
package org.xmodel.external;

import org.xmodel.IModelObject;

/**
 * An interface for an IExternalReference cache.
 */
public interface ICache
{
  /**
   * Called by the preprocessor with the cache configuration fragment.
   * @param annotation The annotation.
   */
  public void configure( IModelObject annotation);
  
  /**
   * Add an IExternalReference to the cache.
   * @param reference The reference to be added.
   */
  public void add( IExternalReference reference);
  
  /**
   * Remove an IExternalReference from the cache.
   * @param reference The reference to be removed.
   */
  public void remove( IExternalReference reference);

  /**
   * Mark the specified IExternalReference accessed.
   * @param reference The reference which was accessed.
   */
  public void touch( IExternalReference reference);
  
  /**
   * Returns the number of IExternalReference instances in the cache.
   * @return Returns the number of IExternalReference instances in the cache.
   */
  public int size();
  
  /**
   * Returns the maximum size of the cache.  The maximum size may increase if an IExternalReference
   * is added to the cache, the cache is full and all the references in the cache are locked.
   * @return Returns the maximum size of the cache.
   */
  public int capacity();
}
