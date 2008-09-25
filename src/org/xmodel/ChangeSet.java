/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.xmodel.record.AddChildBoundRecord;
import org.xmodel.record.ChangeAttributeBoundRecord;
import org.xmodel.record.ClearAttributeBoundRecord;
import org.xmodel.record.RemoveChildBoundRecord;


/**
 * A base implementation of IChangeSet which maintains a list of IChangeRecord entries, creates
 * appropriate entries for each method and provides an implementation of the normalize method. This
 * class also implements IModelListener so that it can automatically track changes to the
 * IModelObjects where it is installed.
 * <p>
 * By default, this implementation ignores the difference between null text and empty text. This
 * is equivalent to assuming that every element has a text node. This behavior can be disabled
 * by calling the <code>regardNullText</code> method.
 */
public class ChangeSet implements IChangeSet, IModelListener
{
  /* (non-Javadoc)
   * @see org.xmodel.IChangeSet#applyChanges()
   */
  public void applyChanges()
  {
    if ( records == null) return;
    for ( int i=0; i<records.size(); i++)
    {
      IBoundChangeRecord record = (IBoundChangeRecord)records.get( i);
      record.applyChange();
    }
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IChangeSet#applyChanges(org.xmodel.IModelObject)
   */
  public void applyChanges( IModelObject object)
  {
    List<IChangeRecord> records = getUnboundRecords();
    for( IChangeRecord record: records) record.applyChange( object);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IChangeSet#clearChanges()
   */
  public void clearChanges()
  {
    records = null;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IChangeSet#setAttribute(org.xmodel.IModelObject, 
   * java.lang.String, java.lang.Object)
   */
  public void setAttribute( IModelObject object, String attrName, Object attrValue)
  {
    if ( !regardNullText && attrName.length() == 0)
    {
      Object oldValue = object.getValue();
      if ( attrValue == null && oldValue != null && oldValue.toString().length() == 0) return;
      if ( oldValue == null && attrValue != null && attrValue.toString().length() == 0) return;
    }
    
    IBoundChangeRecord record = null;
    if ( attrValue.equals( ""))
      record = new ChangeAttributeBoundRecord( object, attrName);
    else
      record = new ChangeAttributeBoundRecord( object, attrName, attrValue);
    addRecord( record);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IChangeSet#removeAttribute(org.xmodel.IModelObject, java.lang.String)
   */
  public void removeAttribute( IModelObject object, String attrName)
  {
    // need to perform this test here because removeAttribute doesn't have oldValue
    if ( !regardNullText && attrName.length() == 0)
    {
      Object oldValue = object.getValue();
      if ( oldValue != null && oldValue.toString().length() == 0) return;
    }
    
    IBoundChangeRecord record = new ClearAttributeBoundRecord( object, attrName);
    addRecord( record);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IChangeSet#addChild(org.xmodel.IModelObject, 
   * org.xmodel.IModelObject)
   */
  public void addChild( IModelObject object, IModelObject child)
  {
    IBoundChangeRecord record = new AddChildBoundRecord( object, child);
    addRecord( record);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IChangeSet#addChild(org.xmodel.IModelObject, 
   * org.xmodel.IModelObject, int)
   */
  public void addChild( IModelObject object, IModelObject child, int index)
  {
    IBoundChangeRecord record = new AddChildBoundRecord( object, child, index);
    addRecord( record);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IChangeSet#removeChild(org.xmodel.IModelObject, 
   * org.xmodel.IModelObject)
   */
  public void removeChild( IModelObject object, IModelObject child)
  {
    IBoundChangeRecord record = new RemoveChildBoundRecord( object, child);
    addRecord( record);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IChangeSet#removeChild(org.xmodel.IModelObject, 
   * org.xmodel.IModelObject, int)
   */
  public void removeChild( IModelObject object, IModelObject child, int index)
  {
    IBoundChangeRecord record = new RemoveChildBoundRecord( object, child, index);
    addRecord( record);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IChangeSet#normalize()
   */
  public void normalize()
  {
    throw new RuntimeException( "Not implemented.");
  }
  
  /**
   * Specify whether the change set should ignore the difference between null text and empty text.
   * @param regard True if null text should be treated as different from empty text.
   */
  public void regardNullText( boolean regard)
  {
    regardNullText = regard;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IChangeSet#addRecord(org.xmodel.IBoundChangeRecord)
   */
  public void addRecord( IBoundChangeRecord record)
  {
    if ( records == null) records = new ArrayList<IBoundChangeRecord>();
    records.add( record);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IChangeSet#removeRecord(org.xmodel.IBoundChangeRecord)
   */
  public void removeRecord( IBoundChangeRecord record)
  {
    if ( records != null) records.remove( record);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IChangeSet#getRecords()
   */
  public List<IBoundChangeRecord> getRecords()
  {
    if ( records == null) return Collections.emptyList();
    return records;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IChangeSet#getUnboundRecords()
   */
  public List<IChangeRecord> getUnboundRecords()
  {
    if ( records == null) return Collections.emptyList();
    List<IChangeRecord> result = new ArrayList<IChangeRecord>( records.size());
    for( IBoundChangeRecord record: records) result.add( record.createUnboundRecord());
    return result;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IChangeSet#getSize()
   */
  public int getSize()
  {
    return (records != null)? records.size(): 0;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IChangeSet#createUndoSet()
   */
  public IChangeSet createUndoSet()
  {
    ChangeSet undoSet = new ChangeSet();
    if ( records != null)
    {
      for( IBoundChangeRecord record: records)
        undoSet.addRecord( record.createUndoRecord());
    }
    return undoSet;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyAdd(org.xmodel.IModelObject, 
   * org.xmodel.IModelObject, int)
   */
  public void notifyAddChild( IModelObject parent, IModelObject child, int index)
  {
    addChild( parent, child, index);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyChange(org.xmodel.IModelObject, 
   * java.lang.String, java.lang.Object, java.lang.Object)
   */
  public void notifyChange( IModelObject object, String attrName, Object newValue, Object oldValue)
  {
    // need to perform this test here because setAttribute doesn't have oldValue
    if ( !regardNullText && attrName.length() == 0)
    {
      if ( newValue == null && oldValue != null && oldValue.toString().length() == 0) return;
      if ( oldValue == null && newValue != null && newValue.toString().length() == 0) return;
    }
    
    setAttribute( object, attrName, newValue);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyClear(org.xmodel.IModelObject, 
   * java.lang.String, java.lang.Object)
   */
  public void notifyClear( IModelObject object, String attrName, Object oldValue)
  {
    // need to perform this test here because removeAttribute doesn't have oldValue
    if ( !regardNullText && attrName.length() == 0)
    {
      if ( oldValue != null && oldValue.toString().length() == 0) return;
    }
    
    removeAttribute( object, attrName);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyParent(org.xmodel.IModelObject, 
   * org.xmodel.IModelObject, org.xmodel.IModelObject)
   */
  public void notifyParent( IModelObject child, IModelObject newParent, IModelObject oldParent)
  {
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyRemove(org.xmodel.IModelObject, 
   * org.xmodel.IModelObject)
   */
  public void notifyRemoveChild( IModelObject parent, IModelObject child, int index)
  {
    removeChild( parent, child);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyDirty(org.xmodel.IModelObject, boolean)
   */
  public void notifyDirty( IModelObject object, boolean dirty)
  {
    // resync
    if ( dirty) object.getChildren();
  }

  /* (non-Javadoc)
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    applyChanges();
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    int size = getSize();
    StringBuilder builder = new StringBuilder();
    builder.append( "records=");
    builder.append( size);
    builder.append( "\n");
    if ( records != null)
    {
      for( IBoundChangeRecord record: records)
      {
        builder.append( record.toString());
        builder.append( "\n");
      }
    }
    return builder.toString();
  }
  
  protected List<IBoundChangeRecord> records;
  private boolean regardNullText;
}
