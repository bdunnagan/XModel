/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * IListenerChain.java
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
package org.xmodel.path;

import org.xmodel.INode;
import org.xmodel.IPath;
import org.xmodel.IPathListener;
import org.xmodel.xpath.expression.IContext;

/**
 * An interface for a chain of listeners used to implement the IPathListener mechanism.  A listener chain
 * consists of an ordered list of IListenerChainLink instances which correspond to the elements of an IPath.
 */
public interface IListenerChain
{
  /**
   * Returns the array of listeners (the chain).
   * @return Returns the array of listeners (the chain).
   */
  public IListenerChainLink[] getLinks();

  /**
   * Returns the IPath to which the IListenerChain belongs.
   * @return Returns the IPath to which the IListenerChain belongs.
   */
  public IPath getPath();

  /**
   * Returns the context of the path evaluation (the bound context).
   * @return Returns the context of the path evaluation.
   */
  public IContext getContext();

  /**
   * Returns the client IPathListener.
   * @return Returns the client IPathListener.
   */
  public IPathListener getPathListener();

  /**
   * Install the first link in the listener chain on the specified object.
   * @param object The object where the listener chain will be installed.
   */
  public void install( INode object);
  
  /**
   * Uninstall the first link in the listener chain on the specified object.
   * @param object The object where the listener chain will be uninstalled.
   */
  public void uninstall( INode object);
}
