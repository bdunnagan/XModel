/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.memento;

import org.xmodel.IModelObject;

/**
 * An IMemento for the updates which involve setting the parent.
 */
public class SetParentMemento implements IMemento
{
  /* (non-Javadoc)
   * @see org.xmodel.memento.IMemento#revert()
   */
  public void revert()
  {
    child.revertUpdate( this);
  }

  /* (non-Javadoc)
   * @see org.xmodel.memento.IMemento#restore()
   */
  public void restore()
  {
    child.restoreUpdate( this);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.memento.IMemento#clear()
   */
  public void clear()
  {
    child = null;
    newParent = null;
    oldParent = null;
  }
  
  public IModelObject child;
  public IModelObject newParent;
  public IModelObject oldParent;
}
