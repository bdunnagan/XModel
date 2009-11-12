/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * UndoListener.java
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
package org.xmodel.listeners;

import org.xmodel.IChangeSet;
import org.xmodel.IModelListener;
import org.xmodel.IModelObject;

/**
 * An implementation of IModelListener that populates an IChangeSet with operations that will 
 * revert changes from the point that the listener was installed. At any point the IChangeSet
 * can be cleared so that a new baseline can be established.
 * <p>
 * The <code>notifyParent</code> method is not implemented per the contract that this change
 * set will only contain records for the object on which it is installed.
 */
public class UndoListener implements IModelListener
{
  /**
   * Create an UndoListener that will populate the specified change set with undo records.
   * @param undoSet The undo change set.
   */
  public UndoListener( IChangeSet undoSet)
  {
    this.undoSet = undoSet;
  }
  
  /**
   * Create an UndoListener that will populate the specified change sets with both undo and redo records.
   * @param undoSet The undo change set.
   * @param redoSet The redo change set.
   */
  public UndoListener( IChangeSet undoSet, IChangeSet redoSet)
  {
    this.undoSet = undoSet;
    this.redoSet = redoSet;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyParent(org.xmodel.IModelObject, org.xmodel.IModelObject, org.xmodel.IModelObject)
   */
  public void notifyParent( IModelObject child, IModelObject newParent, IModelObject oldParent)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyAddChild(org.xmodel.IModelObject, org.xmodel.IModelObject, int)
   */
  public void notifyAddChild( IModelObject parent, IModelObject child, int index)
  {
    undoSet.removeChild( parent, child);
    if ( redoSet != null) redoSet.addChild( parent, child, index);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyRemoveChild(org.xmodel.IModelObject, org.xmodel.IModelObject, int)
   */
  public void notifyRemoveChild( IModelObject parent, IModelObject child, int index)
  {
    undoSet.addChild( parent, child, index);
    if ( redoSet != null) redoSet.removeChild( parent, child);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyChange(org.xmodel.IModelObject, java.lang.String, java.lang.Object, java.lang.Object)
   */
  public void notifyChange( IModelObject object, String attrName, Object newValue, Object oldValue)
  {
    undoSet.setAttribute( object, attrName, oldValue);
    if ( redoSet != null) redoSet.setAttribute( object, attrName, newValue);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyClear(org.xmodel.IModelObject, java.lang.String, java.lang.Object)
   */
  public void notifyClear( IModelObject object, String attrName, Object oldValue)
  {
    undoSet.setAttribute( object, attrName, oldValue);
    if ( redoSet != null) redoSet.removeAttribute( object, attrName);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyDirty(org.xmodel.IModelObject, boolean)
   */
  public void notifyDirty( IModelObject object, boolean dirty)
  {
  }
  
  private IChangeSet undoSet;
  private IChangeSet redoSet;
}
