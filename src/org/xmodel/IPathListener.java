/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel;

import java.util.List;
import org.xmodel.xpath.expression.IContext;


/**
 * An interface for notification for the addition and removal of domain objects with a specified
 * model path. The notification happens regardless of how the object is added to or removed from the
 * model. The path may contain elements with any supported axis including ANCESTOR and DECENDENT.
 */
public interface IPathListener
{
  /**
   * Called when one or more domain objects are added to a layer of the specified path.
   * @param context The context.
   * @param path The path.
   * @param pathIndex The layer index.
   * @param nodes The nodes which were added.
   */
  public void notifyAdd( IContext context, IPath path, int pathIndex, List<IModelObject> nodes);

  /**
   * Called when one or more domain objects are removed from a layer of the specified path.
   * @param context The context.
   * @param path The path.
   * @param pathIndex The layer index.
   * @param nodes The nodes which were removed.
   */
  public void notifyRemove( IContext context, IPath path, int pathIndex, List<IModelObject> nodes);
}
