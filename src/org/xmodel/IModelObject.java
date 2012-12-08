/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * IModelObject.java
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

import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.xmodel.memento.IMemento;

/**
 * The interface for objects in a hierarchical data model suitable for representing the content of
 * an XML document. Each element has one parent, zero or more attributes and zero or more children.
 * An attribute is a name/value pair where the name is any Java String and the value is any Java
 * Object. The attribute whose name is the empty string may also be accessed by calling the
 * <code>getValue</code> method. An attribute which exists but has not been assigned a value will
 * contain an empty string.
 * <p>
 * The interface supports the listener pattern. A listener implements the IModelListener interface
 * and is registered with an IModelObject via the <code>addModelListener</code> method. The
 * listener receives the following notifications:
 * <ul>
 * <li>Notification when a child is added or removed from the object.
 * <li>Notification when an attribute is set, modified or cleared on the object.
 * <li>Notification when the parent of the object changes.
 * </ul>
 * <p>
 * The interface supports cloning of individual objects as well as entire subtrees. See the
 * <code>cloneTree</code> and <code>cloneObject</code> methods for more information.
 */
public interface IModelObject
{
  /**
   * Clear the cached IModel, if present.  This method must be called before an object is accessed
   * by a different thread, since the IModel for the previous thread may be cached.
   */
  public void clearModel();
  
  /**
   * Returns the IModel to which this object belongs. Implementations should use the IModelRegistry
   * to find the IModel. Since using the IModelRegistry involves accessing thread-local data, the
   * IModel should usually be cached.
   * @return Returns the IModel to which this object belongs.
   */
  public IModel getModel();
  
  /**
   * Set the id of the object.
   * @param id The object id.
   */
  public void setID( String id);
  
  /**
   * Returns the id of the object.
   * @return Returns the id of the object.
   */
  public String getID();
  
  /**
   * Get the type of this object.
   * @return Returns the type of this object.
   */
  public String getType();
  
  /**
   * Returns true if this object has the specified type.
   * @param type The object type to test.
   * @return Returns true if this object has the specified type.
   */
  public boolean isType( String type);

  /**
   * Returns true if this object is an external reference and has not been touched yet.
   * @return Returns true if this object is an external reference and has not been touched yet.
   */
  public boolean isDirty();
  
  /**
   * Set an attribute on this object with an empty value.  This method provides 
   * a consistent means of specifying attributes which do not have values.  If 
   * the attribute had a previous value, that value is returned.
   * @param attrName The name of the attribute.
   * @return Returns null or the previous value of the attribute.
   */
  public Object setAttribute( String attrName);
  
  /**
   * Set an attribute on this object. If the attribute had a previous value, 
   * that value is returned.
   * @param attrName The name of the attribute.
   * @param attrValue The value of the attribute.
   * @return Returns null or the previous value of the attribute.
   */
  public Object setAttribute( String attrName, Object attrValue);
  
  /**
   * Get the value of an attribute by name.
   * @param attrName The name of the attribute.
   * @return Returns null or the value of the named attribute.
   */
  public Object getAttribute( String attrName);

  /**
   * Returns an IModelObject whose value is the value of the specified attribute. Listeners can
   * be registered on the attribute IModelObject to receive notification when the attribute 
   * changes, however it is more performant to register an IModelListener on this object and
   * implement the attribute notification methods.
   * @param attrName The name of the attribute.
   * @return Returns an IModelObject whose value is the value of the specified attribute.
   */
  public IModelObject getAttributeNode( String attrName);
  
  /**
   * Return a collection containing the names of all attributes on this object.
   * @return Returns the names of all the attributes.
   */
  public Collection<String> getAttributeNames();
  
  /**
   * Remove the attribute with the specified name from the object.  If the
   * attribute existed, then the previous value of the attribute is returned.
   * @param attrName The name of the attribute.
   * @return Returns null or the previous value of the attribute.
   */
  public Object removeAttribute( String attrName);

  /**
   * Set the single value associated with this object and return the previous value.
   * @param value The value associated with this object.
   * @return Returns the previous value of this object.
   */
  public Object setValue( Object value);

  /**
   * Get the single value associated with this object.
   * @return Returns the single value associated with this object or null.
   */
  public Object getValue();
  
  /**
   * Returns the value of the first child with the specified type.
   * @param type The type of the child.
   * @return Returns the value of the specified child or null if it does not exist.
   */
  public Object getChildValue( String type);
  
  /**
   * Add a child to this object.
   * @param object The object to add as a child.
   */
  public void addChild( IModelObject object);
  
  /**
   * Add a child to this object at the specified index. If the index is -1 the child will be added
   * to the end of the list of children.
   * @param object The object to add as a child.
   * @param index The index where the child will be inserted.
   */
  public void addChild( IModelObject object, int index);
  
  /**
   * Remove the child at the specified index. If the index is -1 the child will be removed from the
   * end of the list of children.
   * @param index The index of the child to be removed.
   * @return Returns the child which was removed.
   */
  public IModelObject removeChild( int index);
  
  /**
   * Remove a child of this object.
   * @param child The child to be removed.
   * @return Returns the object that was removed.
   */
  public void removeChild( IModelObject object);
  
  /**
   * Remove all children from this object.
   */
  public void removeChildren();
  
  /** 
   * Remove all children of the specified type from this object.
   * @param type The type of children to remove.
   */
  public void removeChildren( String type);
  
  /**
   * Remove this object from its parent.
   */
  public void removeFromParent();

  /**
   * Get the child at the specified index.
   * @param index The index of the child.
   * @return Returns the child at the specified index.
   */
  public IModelObject getChild( int index);
  
  /**
   * Get the first child of this object with the specified type.
   * @param type The type of the child.
   * @return Returns the first child with the specified type.
   */
  public IModelObject getFirstChild( String type);
  
  /**
   * Get the first child with the specified name and type.  If the child does not 
   * exists then null is returned.
   * @param type The type of the child.
   * @param name The name of the child.
   * @return Returns the child with the specified type and name.
   */
  public IModelObject getChild( String type, String name);
  
  /**
   * Get the first child with the specified type if it exists, or
   * create it and add it to this object if it doesn't exist.
   * @param type The type of the child.
   * @return Returns the child with the specified type.
   */
  public IModelObject getCreateChild( String type);
  
  /**
   * Get the first child with the specified name and type if it exists, or
   * create it and add it to this object if it doesn't exist.
   * @param type The type of the child.
   * @param name The name of the child.
   * @return Returns the child with the specified type and name.
   */
  public IModelObject getCreateChild( String type, String name);
  
  /**
   * Get the children with the specified name and type.  If a child does not exist
   * then null is returned.
   * @param type The type of the children.
   * @param name The name of the children.
   * @return Returns the children with the specified type and name.
   */
  public List<IModelObject> getChildren( String type, String name);
  
  /**
   * Get all the children of this node.
   * @return Returns all the children of this node.
   */
  public List<IModelObject> getChildren();
  
  /**
   * Get all the children of this node of the specified type.
   * @param type The type of children to return.
   * @return Returns all the children of the specified type.
   */
  public List<IModelObject> getChildren( String type);
  
  /**
   * Returns all the types of children attached to this node.
   * @return Returns the types of children attached to this node.
   */
  public Set<String> getTypesOfChildren();
  
  /**
   * Get the number of children of this node.
   * @return Returns the number of children of this node.
   */
  public int getNumberOfChildren();
  
  /**
   * Get the number of children of this node with the specified type.
   * @param type The type of children.
   * @return Returns the number of children of the specified type.
   */
  public int getNumberOfChildren( String type);
  
  /**
   * Used internally to set the parent of this object. This method should perform the reparent
   * listener notification.  This method should be the last step in the move operation and the
   * first notification given for the listeners of the three objects involved in the move.
   * @param parent The new parent of this object.
   * @return Returns null or the previous parent.
   */
  public IModelObject internal_setParent( IModelObject parent);

  /**
   * Used internally to execute a move operation. A move operation requires the cooperation
   * of three objects and notications can only be performed after the internal state of all
   * three objects has been updated. The methods are provided so that the internal state can
   * be changed without causing notification. 
   * @param child The child to be added.
   */
  public void internal_addChild( IModelObject child, int index);
  
  /**
   * Used internally to execute a move operation. A move operation requires the cooperation
   * of three objects and notications can only be performed after the internal state of all
   * three objects has been updated. The methods are provided so that the internal state can
   * be changed without causing notification. 
   * @param index The index of the child to be removed.
   * @return Returns the child that was removed.
   */
  public IModelObject internal_removeChild( int index);
  
  /**
   * Used internally to execute a move operation. A move operation requires the cooperation
   * of three objects and notications can only be performed after the internal state of all
   * three objects has been updated. The methods are provided so that the internal state can
   * be changed without causing notification. 
   * @param newParent The new parent.
   * @param oldParent The old parent.
   */
  public void internal_notifyParent( IModelObject newParent, IModelObject oldParent);
  
  /**
   * Used internally to execute a move operation. A move operation requires the cooperation
   * of three objects and notications can only be performed after the internal state of all
   * three objects has been updated. The methods are provided so that the internal state can
   * be changed without causing notification. 
   * @param child The child being added.
   * @param index The index of insertion.
   */
  public void internal_notifyAddChild( IModelObject child, int index);
  
  /**
   * Used internally to execute a move operation. A move operation requires the cooperation
   * of three objects and notications can only be performed after the internal state of all
   * three objects has been updated. The methods are provided so that the internal state can
   * be changed without causing notification. 
   * @param child The child being removed.
   * @param index The index of insertion.
   */
  public void internal_notifyRemoveChild( IModelObject child, int index);
  
  /**
   * Return the parent of this object.
   * @return Returns the parent of this object.
   */
  public IModelObject getParent();

  /**
   * Return the first ancestor of this object which has the specified type.
   * @param type The type of the ancestor.
   * @return Returns the first ancestor with the specified type.
   */
  public IModelObject getAncestor( String type);
  
  /**
   * Get the root parent of this object.
   * @return Returns the root parent of this object.
   */
  public IModelObject getRoot();

  /**
   * Add a model listener to this object.
   * @param listener The model listener to be added.
   */
  public void addModelListener( IModelListener listener);
  
  /**
   * Remove a model listener from this object.
   * @param listener The model listener to be removed.
   */
  public void removeModelListener( IModelListener listener);

  /**
   * Returns a list containing all the model listeners attached to this object.
   * @return Returns a list containing all the model listeners attached to this object.
   */
  public ModelListenerList getModelListeners();

  /**
   * Returns the PathListenerList associated with this object. This class is not intended
   * for client use. It is used by IPath implementations to keep track of the IPathListener
   * instances that are installed from this context.
   * @return Returns the PathListenerList associated with this object.
   */
  public PathListenerList getPathListeners();
  
  /**
   * Returns a copy of this object including attributes but not including children.
   * @return Returns a copy of this object.
   */
  public IModelObject cloneObject();
  
  /**
   * Returns a copy of the subtree rooted at this object.
   * @return Returns a copy of the subtree rooted at this object.
   */
  public IModelObject cloneTree();

  /**
   * Returns a new object of the same class as this object.
   * @param type The type of the new object.
   * @return Returns a new object of the same class as this object.
   */
  public IModelObject createObject( String type);
  
  /**
   * If the implementation is a reference to another IModelObject, then this method will return
   * the IModelObject which is the target of this reference. If the implementation is not a 
   * reference, then this method will return this object.
   * @return Returns the object to which this reference refers.
   */
  public IModelObject getReferent();
  
  /**
   * This method will revert the current update to this object. This method has no effect if 
   * it is not called from a listener context. This method should not be called by the client.
   * Instead, the <code>IModel.revertUpdate</code> method should be called instead.
   * @param memento The memento which stores the update information.
   */
  public void revertUpdate( IMemento memento);
  
  /**
   * This method will restore the current update to this object. This method has no effect if 
   * it is not called from a listener context. This method should not be called by the client.
   * Instead, the <code>IModel.restoreUpdate</code> method should be called instead.
   * @param memento The memento which stores the update information.
   */
  public void restoreUpdate( IMemento memento);
}
