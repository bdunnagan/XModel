/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.memento;

/**
 * An interface for a memento of a discreet xmodel update. There a five implementations of this
 * interface for the five types of updates:
 * <ul>
 * <li>SetAttributeMemento
 * <li>RemoveAttributeMemento
 * <li>AddChildMemento
 * <li>RemoveChildMemento
 * <li>VariableMemento
 * </ul>
 * When the <code>revert</code> method is called, the effect of the update is undone without calling
 * any listeners. When the <code>restore</code> method is called, the effect of the update is restored
 * without calling listeners.
 */
public interface IMemento
{
  /**
   * Silently revert this update.
   */
  public void revert();

  /**
   * Silently restore this update.
   */
  public void restore();
  
  /**
   * Clear the data for this memento.
   */
  public void clear();
}
