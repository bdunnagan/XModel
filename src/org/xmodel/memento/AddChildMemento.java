/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.memento;

import org.xmodel.IModelObject;

/**
 * An IMemento for the updates which involve adding a child.
 */
public class AddChildMemento implements IMemento
{
  /* (non-Javadoc)
   * @see org.xmodel.memento.IMemento#revert()
   */
  public void revert()
  {
    if ( parent != null)
      parent.revertUpdate( this);
  }

  /* (non-Javadoc)
   * @see org.xmodel.memento.IMemento#restore()
   */
  public void restore()
  {
    if ( parent != null)
      parent.restoreUpdate( this);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.memento.IMemento#clear()
   */
  public void clear()
  {
    parent = null;
    child = null;
    index = 0;
  }
  
  public IModelObject parent;
  public IModelObject child;
  public int index;
}
