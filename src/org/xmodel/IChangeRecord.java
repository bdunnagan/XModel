/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel;

/**
 * An interface which describes a specific change to one object in a model. The <code>getPath</code>
 * method returns the path of the target of the change. The path may be absolute or relative.
 */
public interface IChangeRecord
{
  /**
   * Constant type of an attribute modification record.
   */
  public final static int CHANGE_ATTRIBUTE = 0;
  
  /**
   * Constant type of an attribute delete record.
   */
  public final static int CLEAR_ATTRIBUTE = 1;
  
  /**
   * Constant type of an add child record.
   */
  public final static int ADD_CHILD = 2;
  
  /**
   * Constant type of a delete child record.
   */
  public final static int REMOVE_CHILD = 3;
  
  /**
   * Get the type of change record, one of: SET_ATTRIBUTE, REMOVE_ATTRIBUTE,
   * ADD_CHILD or REMOVE_CHILD.
   * @return Returns the type of change record.
   */
  public int getType();
  
  /**
   * Returns true if this change record is of the specified type.
   * @param type The type of the change record.
   * @return Returns true if this change record is of the specified type.
   */
  public boolean isType( int type);

  /**
   * Returns the path for the domain object affected by this change record.
   * @return Returns the path in the domain model.
   */
  public IPath getPath();

  /**
   * Get the name of the modified or deleted attribute.
   * @return Returns the name of the modified or deleted attribute.
   */
  public String getAttributeName();
  
  /**
   * Get the new value of the modified attribute.
   * @return Returns the new value of the modified attribute.
   */
  public Object getAttributeValue();
  
  /**
   * Get the child domain object which was added or deleted.
   * @return Returns the child object which was added or deleted.
   */
  public IModelObject getChild();
  
  /**
   * Get the index of the child to be added or removed.
   * @return Returns the index of the child to be added or removed.
   */
  public int getIndex();
  
  /**
   * Apply this change record to the target object found by evaluating the path returned by the
   * <code>getPath</code> method from the specified root context.
   * @param root The root of the subtree where this change will be applied.
   */
  public void applyChange( IModelObject root);
}
