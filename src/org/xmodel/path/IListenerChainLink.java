/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * IListenerChainLink.java
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

import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.xpath.expression.IContext;


/**
 * An interface for a member of a chain of objects, corresponding to the location steps of an IPath,
 * which implement the IPathListener notification mechanism.
 */
public interface IListenerChainLink
{
  /**
   * Called when the listener chain is bound. This method is provided so that PredicateGuard can
   * know when to install its listener on its predicate expression.
   * @param context The context.
   */
  public void bind( IContext context);
  
  /**
   * Called when the listener chain is unbound. This method is provided so that PredicateGuard can
   * know when to uninstall its listener from its predicate expression.
   * @param context The context.
   */
  public void unbind( IContext context);
  
  /**
   * Install the chain segment beginning with this IListenerChainLink on the objects in the given
   * list. The list is assumed to contain all of the objects which lie on this layer of the path.
   * However, the list may contain additional objects which do not lie on the path.
   * @param list A list containing objects belonging to this layer.
   */
  public void install( List<IModelObject> list);
  
  /**
   * Uninstall the chain segment beginning with this IListenerChainLink on the objects in the given
   * list. The list is assumed to contain all of the objects which lie on this layer of the path.
   * However, the list may contain additional objects which do not lie on the path.
   * @param list A list containing objects belonging to this layer.
   */
  public void uninstall( List<IModelObject> list);
  
  /**
   * Install the chain segment beginning with this IListenerChainLink on the specified object.
   * @param object The object where this listener will be installed.
   */
  public void incrementalInstall( IModelObject object);
  
  /**
   * Uninstall the chain segment beginning with this IListenerChainLink from the specified object.
   * @param object The object where this listener will be installed.
   */
  public void incrementalUninstall( IModelObject object);
  
  /**
   * Install the chain segment beginning with this IListenerChainLink on the specified objects.
   * @param objects The objects where this listener will be installed.
   */
  public void incrementalInstall( List<IModelObject> objects);
  
  /**
   * Uninstall the chain segment beginning with this IListenerChainLink from the specified objects.
   * @param objects The objects where this listener will be installed.
   */
  public void incrementalUninstall( List<IModelObject> objects);
  
  /**
   * Returns the chain of which this link is a part.
   * @return Returns the chain of which this link is a part.
   */
  public IListenerChain getListenerChain();
  
  /**
   * Returns the path index of this link.
   * @return Returns the path index of this link.
   */
  public int getPathIndex();
  
  /**
   * Returns a clone for the specified chain.
   * @param next The next link.
   * @return Returns a clone for the specified chain.
   */
  public IListenerChainLink cloneOne( IListenerChain chain);
}
