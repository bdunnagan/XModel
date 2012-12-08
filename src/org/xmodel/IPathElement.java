/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * IPathElement.java
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


public interface IPathElement
{
  /**
   * Set the parent path.
   * @param path The parent path;
   */
  public void setParent( IPath path);
  
  /**
   * Return the axis for this location as defined in IAxis.
   * @return Returns the axis for this location.
   */
  public int axis();
  
  /**
   * Set the axis for this element (defined in IAxis).
   * @param axis The axis.
   */
  public void setAxis( int axis);
  
  /**
   * Returns true if this element has the given axis.
   * @param axis The axis for which to test.
   * @return Returns true if this element has the given axis.
   */
  public boolean hasAxis( int axis);
  
  /**
   * Returns the object type for this location step.
   * @return Returns the object type for this location step.
   */
  public String type();

  /**
   * Returns the predicate for this element or null if there is none.
   * @return Returns the predicate for this element or null if there is none.
   */
  public IPredicate predicate();

  /**
   * Filter the specified node-set, which must consists of nodes on the axis of this element.
   * The node-set may not be the complete node-set that would have been returned by the path.
   * The path, and the index of the path step, are provided so that the complete candidate
   * node-set can be evaluated, if required.
   * This method is provided so that FanoutListeners do not have to revert the model when
   * it isn't necessary.
   * @param parent The parent context.
   * @param path The path to which this step belongs.
   * @param step The index of this step.
   * @param nodes The nodes to be filtered.
   */
  public void filter( IContext parent, IPath path, int step, List<IModelObject> nodes);
  
  /**
   * Returns the list of objects which are selected by this path element when the specified object
   * is the current location. If the result argument is not null, then the selected objects are
   * appended to that list.
   * @param parent The parent context or null.
   * @param object The current location.
   * @param result The list where the result will be stored or null.
   * @return Returns a list containing the selected objects.
   */
  public List<IModelObject> query( IContext parent, IModelObject object, List<IModelObject> result);

  /**
   * Return the results of applying this query to each object in the specified list.
   * @param parent The parent context or null.
   * @param list The list of objects to query.
   * @param result Null or the result list.
   */
  public List<IModelObject> query( IContext parent, List<IModelObject> list, List<IModelObject> result);
  
  /**
   * Clone this element.
   * @return Returns the cloned element.
   */
  public IPathElement clone();
  
  /**
   * Clone this element but use the specified axis.
   * @param axis The new axis.
   * @return Returns the cloned element.
   */
  public IPathElement clone( int axis);
}
