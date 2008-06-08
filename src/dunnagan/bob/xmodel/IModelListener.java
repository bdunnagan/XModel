/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel;

/**
 * An interface for receiving notifications of changes to IModelObject instances. This interface is
 * the most basic form of notification and is the basis for all the other listener notification
 * mechanisms (e.g. IPathListener). When a child is added to or removed from a parent IModelObject,
 * there are two notifications generated: first, the child is notified that its parent has changed,
 * and then the parent is notified that a child was added or removed. Performing the notifications
 * in this order gives listeners on the child an opportunity to finish configuring the child before
 * it is added to the model.
 * <p>
 * IModelListener is notified of changes to the value of an IModelObject by calling the 
 * <code>notifyChange</code> method with an empty string for the attribute name.  The value of an
 * object is the result of calling the <code>getValue</code> method on the IModelObject.  The value
 * is usually used to store the text node of an XML element.
 * <p>
 * The IModelObject interface guarantees that each IModelListener will receive notification of all
 * changes made to any object with which it is registered.  To make this possible, an IModelObject
 * implementation must defer changes which are made to it during listener notification.  The object
 * will not change state until after all listeners have been notified.
 */
public interface IModelListener
{
  /**
   * Called when the parent of a domain object is set.  This method is called before
   * the corresponding notifyAdd and/or notifyRemove on the child.
   * @param child The domain object whose parent was changed.
   * @param newParent The new parent or null if the object is being detached.
   * @param oldParent The old parent or null if the object is being attached.
   */
  public void notifyParent( IModelObject child, IModelObject newParent, IModelObject oldParent);
  
  /**
   * Called when a child is added to a domain object. If the child was added to the end of the list
   * then the index will be -1.
   * @param parent The parent to which the child was added.
   * @param child The child that was added.
   */
  public void notifyAddChild( IModelObject parent, IModelObject child, int index);
  
  /**
    Called just before the child is removed from a domain object.
   * @param parent The parent from which the child will be removed.
   * @param child The child that will be removed.
   * @param index The index of the child which will be removed.
  */
  public void notifyRemoveChild( IModelObject parent, IModelObject child, int index);
  
  /**
    Called when an attribute on a domain object is changed or when the value of an object
    changes.  In the latter case, the attrName argument will be an empty string.
    @param object The domain object whose attribute was changed.
    @param attrName The attribute which was changed.
    @param newValue The new value of the attribute.
    @param oldValue The original value of the attribute.
  */
  public void notifyChange( IModelObject object, String attrName, Object newValue, Object oldValue);

  /**
    Called when an attribute on a domain object is cleared or when the value of an object
    is cleared.  In the latter case, the attrName argument will be an empty string.
    @param object The domain object whose attribute was changed.
    @param attrName The attribute which was changed.
    @param oldValue The original value of the attribute.
  */
  public void notifyClear( IModelObject object, String attrName, Object oldValue);
}
