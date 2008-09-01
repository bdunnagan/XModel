/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
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
  public void notifyAttach( IModelObject object, IModelObject ancestor, IModelObject newParent, IModelObject oldParent);

  /**
   * This method is called when the specified ancestor of the specified object
   * is disconnected from the parent object.
   * @param object The decendant of the ancestor.
   * @param ancestor The ancestor of the specified object.
   * @param oldParent The old parent of the ancestor.
   */
  public void notifyDetach( IModelObject object, IModelObject ancestor, IModelObject oldParent);
}
