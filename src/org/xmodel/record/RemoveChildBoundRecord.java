/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * RemoveChildBoundRecord.java
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
package org.xmodel.record;

import org.xmodel.*;

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
   * @see org.xmodel.IBoundChangeRecord#createUndoRecord()
   */
  public IBoundChangeRecord createUndoRecord()
  {
    if ( index < 0)
      return new AddChildBoundRecord( object, child);
    else
      return new AddChildBoundRecord( object, child, index);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IBoundChangeRecord#createUnboundRecord()
   */
  public IChangeRecord createUnboundRecord()
  {
    IModelObject child = this.child;
    if ( child == null) child = getBoundObject().getChild(  index);
    IPath childPath = ModelAlgorithms.createRelativePath( getBoundObject(), child);
    return new RemoveChildRecord( getPath(), childPath);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IBoundChangeRecord#createUnboundRecord(org.xmodel.IModelObject)
   */
  public IChangeRecord createUnboundRecord( IModelObject relative)
  {
    IModelObject child = this.child;
    if ( child == null) child = getBoundObject().getChild(  index);
    IPath childPath = ModelAlgorithms.createRelativePath( getBoundObject(), child);
    return new RemoveChildRecord( getRelativePath( relative), childPath);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IChangeRecord#getType()
   */
  public int getType()
  {
    return REMOVE_CHILD;
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
    {
      getBoundObject().removeChild( child); 
    }
    else if ( child != null)
    {
      // use index but ensure it is correct
      IModelObject bound = getBoundObject();
      IModelObject object = bound.getChild( index);
      if ( object.equals( child)) bound.removeChild( index); else bound.removeChild( child);
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
