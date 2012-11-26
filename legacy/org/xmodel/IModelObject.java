package org.xmodel;
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

import java.util.List;
import java.util.Set;

/**
 * Legacy interface for a model node. 
 */
public interface IModelObject extends INode
{
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
   * Set an attribute on this object with an empty value.  This method provides 
   * a consistent means of specifying attributes which do not have values.  If 
   * the attribute had a previous value, that value is returned.
   * @param attrName The name of the attribute.
   * @return Returns null or the previous value of the attribute.
   */
  public Object setAttribute( String attrName);
  
  /**
   * Returns the value of the first child with the specified type.
   * @param type The type of the child.
   * @return Returns the value of the specified child or null if it does not exist.
   */
  public Object getChildValue( String type);
  
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
   * Get the first child with the specified name and type.  If the child does not 
   * exists then null is returned.
   * @param type The type of the child.
   * @param name The name of the child.
   * @return Returns the child with the specified type and name.
   */
  public INode getChild( String type, String name);
  
  /**
   * Get the first child with the specified name and type if it exists, or
   * create it and add it to this object if it doesn't exist.
   * @param type The type of the child.
   * @param name The name of the child.
   * @return Returns the child with the specified type and name.
   */
  public INode getCreateChild( String type, String name);
  
  /**
   * Get the children with the specified name and type.  If a child does not exist
   * then null is returned.
   * @param type The type of the children.
   * @param name The name of the children.
   * @return Returns the children with the specified type and name.
   */
  public List<INode> getChildren( String type, String name);
  
  /**
   * Returns all the types of children attached to this node.
   * @return Returns the types of children attached to this node.
   */
  public Set<String> getTypesOfChildren();
  
  /**
   * Get the number of children of this node with the specified type.
   * @param type The type of children.
   * @return Returns the number of children of the specified type.
   */
  public int getNumberOfChildren( String type);
  
  /**
   * Return the first ancestor of this object which has the specified type.
   * @param type The type of the ancestor.
   * @return Returns the first ancestor with the specified type.
   */
  public INode getAncestor( String type);
  
  /**
   * Get the root parent of this object.
   * @return Returns the root parent of this object.
   */
  public INode getRoot();

  /**
   * Add a ancestor listener to this object.
   * @param listener The model listener to be added.
   */
  public void addAncestorListener( IAncestorListener listener);
  
  /**
   * Remove a ancestor listener from this object.
   * @param listener The model listener to be removed.
   */
  public void removeAncestorListener( IAncestorListener listener);
  
  /**
   * Returns a new object of the same class as this object.
   * @param type The type of the new object.
   * @return Returns a new object of the same class as this object.
   */
  public INode createObject( String type);
  
  /**
   * If the implementation is a reference to another IModelObject, then this method will return
   * the IModelObject which is the target of this reference. If the implementation is not a 
   * reference, then this method will return this object.
   * @return Returns the object to which this reference refers.
   */
  public INode getReferent();
}
