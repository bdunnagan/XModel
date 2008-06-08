/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.record;

import dunnagan.bob.xmodel.*;

/**
 * An implementation of IBoundChangeRecord for removing children.
 */
public class RemoveChildBoundRecord extends AbstractBoundRecord
{
  /**
   * Create a new bound change record for removing the specified child.
   * @param object The object whose child will be removed.
   * @param child The child to be removed.
   */
  public RemoveChildBoundRecord( IModelObject object, IModelObject child)
  {
    super( object);
    this.child = child;
    //FIXME: index should not be -1 or undo will not work
    this.index = -1;
  }
  
  /**
   * Create a new bound change record for removing the child at the specified index.
   * @param object The object whose child will be removed.
   * @param child The child to be removed.
   * @param index The index of the child to be removed.
   */
  public RemoveChildBoundRecord( IModelObject object, IModelObject child, int index)
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
    if ( index < 0)
      return new AddChildBoundRecord( object, child);
    else
      return new AddChildBoundRecord( object, child, index);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IBoundChangeRecord#createUnboundRecord()
   */
  public IChangeRecord createUnboundRecord()
  {
    IModelObject child = this.child;
    if ( child == null) child = getBoundObject().getChild(  index);
    IPath childPath = ModelAlgorithms.createRelativePath( getBoundObject(), child);
    return new RemoveChildRecord( getPath(), childPath);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IBoundChangeRecord#createUnboundRecord(dunnagan.bob.xmodel.IModelObject)
   */
  public IChangeRecord createUnboundRecord( IModelObject relative)
  {
    IModelObject child = this.child;
    if ( child == null) child = getBoundObject().getChild(  index);
    IPath childPath = ModelAlgorithms.createRelativePath( getBoundObject(), child);
    return new RemoveChildRecord( getRelativePath( relative), childPath);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IChangeRecord#getType()
   */
  public int getType()
  {
    return REMOVE_CHILD;
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
    {
      getBoundObject().removeChild( child); 
    }
    else if ( child != null)
    {
      // use index but ensure it is correct
      IModelObject bound = getBoundObject();
      IModelObject object = bound.getChild( index);
      if ( object == child) bound.removeChild( index); else bound.removeChild( child);
    }
    else
    {
      getBoundObject().removeChild( index);
    }
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    IModelObject parent = getBoundObject();
    if ( index < 0)
      return "remove: child: "+child+" parent: "+parent;
    else
      return "remove: child: "+child+", index: "+index+" parent: "+parent;
  }

  IModelObject child;
  int index;
}
