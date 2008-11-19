/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.record;

import org.xmodel.IBoundChangeRecord;
import org.xmodel.IChangeRecord;
import org.xmodel.IModelObject;

/**
 * An implementation of IBoundChangeRecord for adding children.
 */
public class AddChildBoundRecord extends AbstractBoundRecord
{
  public AddChildBoundRecord( IModelObject object, IModelObject child)
  {
    super( object);
    this.child = child;
    this.index = -1;
  }
  
  public AddChildBoundRecord( IModelObject object, IModelObject child, int index)
  {
    super( object);
    this.child = child;
    this.index = index;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IBoundChangeRecord#createUndoRecord()
   */
  public IBoundChangeRecord createUndoRecord()
  {
    return new RemoveChildBoundRecord( object, child);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IBoundChangeRecord#createUnboundRecord()
   */
  public IChangeRecord createUnboundRecord()
  {
    return new AddChildRecord( getPath(), child, index);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IBoundChangeRecord#createUnboundRecord(org.xmodel.IModelObject)
   */
  public IChangeRecord createUnboundRecord( IModelObject relative)
  {
    return new AddChildRecord( getRelativePath( relative), child, index);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IChangeRecord#getType()
   */
  public int getType()
  {
    return ADD_CHILD;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.record.AbstractChangeRecord#getChild()
   */
  @Override
  public IModelObject getChild()
  {
    return child;
  }

  /* (non-Javadoc)
   * @see org.xmodel.record.AbstractChangeRecord#getIndex()
   */
  @Override
  public int getIndex()
  {
    return index;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IBoundChangeRecord#execute()
   */
  public void applyChange()
  {
    if ( index < 0)
      getBoundObject().addChild( child);
    else
      getBoundObject().addChild( child, index);
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    IModelObject parent = getBoundObject();
    return "add child: "+child+", index: "+index+", parent: "+parent;
  }
  
  IModelObject child;
  int index;
}
