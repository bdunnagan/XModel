/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
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
  public void setAttribute( IModelObject object, String attrName, Object attrValue);
  
  /**
   * Add a bound change record for removing and attribute from a domain object.
   * @param object The bound object.
   * @param attrName The name of the attribute.
   */
  public void removeAttribute( IModelObject object, String attrName);
  
  /**
   * Add a bound change record for adding a child to a domain object. 
   * @param object The bound object.
   * @param child The child to be added.
   */
  public void addChild( IModelObject object, IModelObject child);

  /**
   * Add a bound change record for adding a child to a domain object at the specified index.
   * @param object The bound object.
   * @param child The child to be added.
   * @param index The index where the child should be inserted.
   */
  public void addChild( IModelObject object, IModelObject child, int index);

  /**
   * Add a bound change record for removing a child to a domain object. 
   * @param object The bound object.
   * @param child The child to be removed.
   */
  public void removeChild( IModelObject object, IModelObject child);

  /**
   * Add a bound change record for removing a child to a domain object at the specified index. 
   * @param object The bound object.
   * @param child The child to be removed.
   * @param index The index of the child to be removed.
   */
  public void removeChild( IModelObject object, IModelObject child, int index);

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
  public void applyChanges( IModelObject object);
  
  /**
   * Clear the contents of this IChangeSet.
   */
  public void clearChanges();
}
