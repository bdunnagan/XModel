/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.memento;

import dunnagan.bob.xmodel.IModelObject;

/**
 * An IMemento for the updates which involve setting an attribute.
 */
public class SetAttributeMemento implements IMemento
{
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.memento.IMemento#revert()
   */
  public void revert()
  {
    object.revertUpdate( this);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.memento.IMemento#restore()
   */
  public void restore()
  {
    object.restoreUpdate( this);
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.memento.IMemento#clear()
   */
  public void clear()
  {
    object = null;
    attrName = null;
    newValue = null;
    oldValue = null;
  }
  
  public IModelObject object;
  public String attrName;
  public Object newValue;
  public Object oldValue;
}
