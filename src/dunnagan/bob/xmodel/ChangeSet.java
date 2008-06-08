/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dunnagan.bob.xmodel.record.AddChildBoundRecord;
import dunnagan.bob.xmodel.record.ChangeAttributeBoundRecord;
import dunnagan.bob.xmodel.record.ClearAttributeBoundRecord;
import dunnagan.bob.xmodel.record.RemoveChildBoundRecord;

/**
 * A base implementation of IChangeSet which maintains a list of IChangeRecord entries, creates
 * appropriate entries for each method and provides an implementation of the normalize method. This
 * class also implements IModelListener so that it can automatically track changes to the
 * IModelObjects where it is installed.
 */
public class ChangeSet implements IChangeSet, IModelListener
{
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IChangeSet#applyChanges()
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
   * @see dunnagan.bob.xmodel.IChangeSet#applyChanges(dunnagan.bob.xmodel.IModelObject)
   */
  public void applyChanges( IModelObject object)
  {
    List<IChangeRecord> records = getUnboundRecords();
    for( IChangeRecord record: records) record.applyChange( object);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IChangeSet#clearChanges()
   */
  public void clearChanges()
  {
    records = null;
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IChangeSet#setAttribute(dunnagan.bob.xmodel.IModelObject, 
   * java.lang.String, java.lang.Object)
   */
  public void setAttribute( IModelObject object, String attrName, Object attrValue)
  {
    IBoundChangeRecord record = null;
    if ( attrValue.equals( ""))
      record = new ChangeAttributeBoundRecord( object, attrName);
    else
      record = new ChangeAttributeBoundRecord( object, attrName, attrValue);
    addRecord( record);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IChangeSet#removeAttribute(dunnagan.bob.xmodel.IModelObject, java.lang.String)
   */
  public void removeAttribute( IModelObject object, String attrName)
  {
    IBoundChangeRecord record = new ClearAttributeBoundRecord( object, attrName);
    addRecord( record);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IChangeSet#addChild(dunnagan.bob.xmodel.IModelObject, 
   * dunnagan.bob.xmodel.IModelObject)
   */
  public void addChild( IModelObject object, IModelObject child)
  {
    IBoundChangeRecord record = new AddChildBoundRecord( object, child);
    addRecord( record);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IChangeSet#addChild(dunnagan.bob.xmodel.IModelObject, 
   * dunnagan.bob.xmodel.IModelObject, int)
   */
  public void addChild( IModelObject object, IModelObject child, int index)
  {
    IBoundChangeRecord record = new AddChildBoundRecord( object, child, index);
    addRecord( record);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IChangeSet#removeChild(dunnagan.bob.xmodel.IModelObject, 
   * dunnagan.bob.xmodel.IModelObject)
   */
  public void removeChild( IModelObject object, IModelObject child)
  {
    IBoundChangeRecord record = new RemoveChildBoundRecord( object, child);
    addRecord( record);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IChangeSet#removeChild(dunnagan.bob.xmodel.IModelObject, 
   * dunnagan.bob.xmodel.IModelObject, int)
   */
  public void removeChild( IModelObject object, IModelObject child, int index)
  {
    IBoundChangeRecord record = new RemoveChildBoundRecord( object, child, index);
    addRecord( record);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IChangeSet#normalize()
   */
  public void normalize()
  {
    throw new RuntimeException( "Not implemented.");
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IChangeSet#addRecord(dunnagan.bob.xmodel.IBoundChangeRecord)
   */
  public void addRecord( IBoundChangeRecord record)
  {
    if ( records == null) records = new ArrayList<IBoundChangeRecord>();
    records.add( record);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IChangeSet#removeRecord(dunnagan.bob.xmodel.IBoundChangeRecord)
   */
  public void removeRecord( IBoundChangeRecord record)
  {
    if ( records != null) records.remove( record);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IChangeSet#getRecords()
   */
  public List<IBoundChangeRecord> getRecords()
  {
    if ( records == null) return Collections.emptyList();
    return records;
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IChangeSet#getUnboundRecords()
   */
  public List<IChangeRecord> getUnboundRecords()
  {
    if ( records == null) return Collections.emptyList();
    List<IChangeRecord> result = new ArrayList<IChangeRecord>( records.size());
    for( IBoundChangeRecord record: records) result.add( record.createUnboundRecord());
    return result;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IChangeSet#getSize()
   */
  public int getSize()
  {
    return (records != null)? records.size(): 0;
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelListener#notifyAdd(dunnagan.bob.xmodel.IModelObject, 
   * dunnagan.bob.xmodel.IModelObject, int)
   */
  public void notifyAddChild( IModelObject parent, IModelObject child, int index)
  {
    addChild( parent, child, index);
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelListener#notifyChange(dunnagan.bob.xmodel.IModelObject, 
   * java.lang.String, java.lang.Object, java.lang.Object)
   */
  public void notifyChange( IModelObject object, String attrName, Object newValue, Object oldValue)
  {
    setAttribute( object, attrName, newValue);
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelListener#notifyClear(dunnagan.bob.xmodel.IModelObject, 
   * java.lang.String, java.lang.Object)
   */
  public void notifyClear( IModelObject object, String attrName, Object oldValue)
  {
    removeAttribute( object, attrName);
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelListener#notifyParent(dunnagan.bob.xmodel.IModelObject, 
   * dunnagan.bob.xmodel.IModelObject, dunnagan.bob.xmodel.IModelObject)
   */
  public void notifyParent( IModelObject child, IModelObject newParent, IModelObject oldParent)
  {
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelListener#notifyRemove(dunnagan.bob.xmodel.IModelObject, 
   * dunnagan.bob.xmodel.IModelObject)
   */
  public void notifyRemoveChild( IModelObject parent, IModelObject child, int index)
  {
    removeChild( parent, child);
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
}
