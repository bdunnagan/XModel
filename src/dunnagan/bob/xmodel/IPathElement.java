/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel;

import java.util.List;

import dunnagan.bob.xmodel.xpath.expression.IContext;

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
   * Returns true if the specified object corresponds to this path element.
   * @param context The parent context or null.
   * @param path The path being evaluated.
   * @param candidate The object to be tested.
   * @return Returns true if the specified object matches this path element.
   */
  public boolean evaluate( IContext context, IPath path, IModelObject candidate);
  
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
