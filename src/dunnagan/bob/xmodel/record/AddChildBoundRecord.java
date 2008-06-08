/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.record;

import dunnagan.bob.xmodel.IBoundChangeRecord;
import dunnagan.bob.xmodel.IChangeRecord;
import dunnagan.bob.xmodel.IModelObject;

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
   * @see dunnagan.bob.xmodel.IBoundChangeRecord#createUndoRecord()
   */
  public IBoundChangeRecord createUndoRecord()
  {
    return new RemoveChildBoundRecord( object, child);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IBoundChangeRecord#createUnboundRecord()
   */
  public IChangeRecord createUnboundRecord()
  {
    return new AddChildRecord( getPath(), child, index);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IBoundChangeRecord#createUnboundRecord(dunnagan.bob.xmodel.IModelObject)
   */
  public IChangeRecord createUnboundRecord( IModelObject relative)
  {
    return new AddChildRecord( getRelativePath( relative), child, index);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IChangeRecord#getType()
   */
  public int getType()
  {
    return ADD_CHILD;
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.record.AbstractChangeRecord#getChild()
   */
  @Override
  public IModelObject getChild()
  {
    return child;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.record.AbstractChangeRecord#getIndex()
   */
  @Override
  public int getIndex()
  {
    return index;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IBoundChangeRecord#execute()
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
