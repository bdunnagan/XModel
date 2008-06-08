/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.memento;

import dunnagan.bob.xmodel.xpath.variable.IVariableScope;

public class VariableMemento implements IMemento
{
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.memento.IMemento#revert()
   */
  public void revert()
  {
    scope.revert( this);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.memento.IMemento#restore()
   */
  public void restore()
  {
    scope.restore( this);
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.memento.IMemento#clear()
   */
  public void clear()
  {
    scope = null;
    varName = null;
    newValue = null;
    oldValue = null;
  }

  public IVariableScope scope;
  public String varName;
  public Object newValue;
  public Object oldValue;
}
