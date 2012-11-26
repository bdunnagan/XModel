/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ITransform.java
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
package org.xmodel.xml;

import org.xmodel.INode;

/**
 * An interface for transforming IModelObject instances.
 */
public interface ITransform
{
  /**
   * Transform the subtree rooted on the input argument, find the difference of the result
   * with the output argument subtree and apply the difference change set to the output.
   * @param input The root of the subtree to be transformed.
   * @param output The root of the subtree which will be modified.
   */
  public void transform( INode input, INode output);

  /**
   * Performs the exact inverse transform of the <code>transform</code> method.
   * @param input The root of the subtree to be transformed.
   * @param output The root of the subtree which will be modified.
   */
  public void inverseTransform( INode input, INode output);
  
  /**
   * Transform the subtree rooted on the specified input object and return the root of the new subtree.
   * @param input The root of the subtree to be transformed.
   * @return Returns the root of the transformed input subtree.
   */
  public INode transform( INode input);
  
  /**
   * Performs the exact inverse transform of the <code>transform</code> method.
   * @param input The root of the subtree to be transformed.
   * @return Returns the root of the transformed input subtree.
   */
  public INode inverseTransform( INode input);
  
  /**
   * Returns true if this transform has an inverse transform.
   * @return Returns true if this transform has an inverse transform.
   */
  public boolean hasInverse();
}
