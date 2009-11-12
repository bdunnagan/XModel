/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * IModelPrinter.java
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

import java.io.IOException;
import java.io.OutputStream;

/**
 * An interface for rendering a model whole or in part to an OutputStream.
 */
public interface IModelPrinter
{
  /**
   * Render the subtree rooted at the specified node.
   * @param object The root of the subtree to render.
   * @return Returns a string containing the rendered tree.
   */
  public String renderTree( IModelObject object);
  
  /**
   * Render the specified node.
   * @param object The node to render.
   * @return Returns a string containing the rendered node.
   */
  public String renderObject( IModelObject object);

  /**
   * Render the subtree rooted at the specified node to the specified OutputStream.
   * @param object The root of the subtree to render.
   */
  public void printTree( OutputStream stream, IModelObject object) throws IOException;

  /**
   * Render the specified node to the specified OutputStream.
   * @param object The node to render.
   */
  public void printObject( OutputStream stream, IModelObject object) throws IOException;
}
