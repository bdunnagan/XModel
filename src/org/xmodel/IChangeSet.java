/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * IChangeSet.java
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

/**
 * An interface for accumulating and normalizing bound change records.  In this context,
 * normalize means to remove redundant records and sets of records which result in
 * no changes being made to the domain model.
 */
public interface IChangeSet extends Runnable
{
  /**
   * Add a bound change record for setting an attribute on a domain object.
   * @param object The bound object.
   * @param attrName The name of the attribute.
   * @param attrValue The new value of the attribute.
   */
  public void setAttribute( INode object, String attrName, Object attrValue);
  
  /**
   * Add a bound change record for removing and attribute from a domain object.
   * @param object The bound object.
   * @param attrName The name of the attribute.
   */
  public void removeAttribute( INode object, String attrName);
  
  /**
   * Add a bound change record for adding a child to a domain object. 
   * @param object The bound object.
   * @param child The child to be added.
   */
  public void addChild( INode object, INode child);

  /**
   * Add a bound change record for adding a child to a domain object at the specified index.
   * @param object The bound object.
   * @param child The child to be added.
   * @param index The index where the child should be inserted.
   */
  public void addChild( INode object, INode child, int index);

  /**
   * Add a bound change record for removing a child to a domain object. 
   * @param object The bound object.
   * @param child The child to be removed.
   */
  public void removeChild( INode object, INode child);

  /**
   * Add a bound change record for removing a child to a domain object at the specified index. 
   * @param object The bound object.
   * @param child The child to be removed.
   * @param index The index of the child to be removed.
   */
  public void removeChild( INode object, INode child, int index);

  /**
   * Remove redundant records and sets of records which do not
   * result in changes to the domain model.
   */
  public void normalize();
  
  /**
   * Add an IBoundChangeRecord to this set.
   * @param record The record.
   */
  public void addRecord( IBoundChangeRecord record);
  
  /**
   * Remove an IBoundChangeRecord from this set.
   * @param record The record.
   */
  public void removeRecord( IBoundChangeRecord record);
  
  /**
   * Returns a list containing the records for this change set.  Prior to a call to
   * the normalize method, the records will appear in the order in which they were 
   * added to the change set.
   */
  public List<IBoundChangeRecord> getRecords();
  
  /**
   * Returns an unbound record for each accumulated record.
   * @return Returns an unbound record for each accumulated record.
   */
  public List<IChangeRecord> getUnboundRecords();
  
  /**
   * Returns the number of records in this change set.
   * @return Returns the number of records in this change set.
   */
  public int getSize();

  /**
   * Apply the changes.
   */
  public void applyChanges();
  
  /**
   * Apply the changes to the specified object.
   * @param object
   */
  public void applyChanges( INode object);
  
  /**
   * Clear the contents of this IChangeSet.
   */
  public void clearChanges();
}
