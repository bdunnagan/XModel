/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * Update.java
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
package org.xmodel;

import java.util.ArrayList;
import java.util.List;

import org.xmodel.log.Log;
import org.xmodel.memento.*;
import org.xmodel.xpath.variable.IVariableScope;


/**
 * A class which stores all the IMemento instances involved in an update and provides 
 * convenience methods for creating the mementos. The class also assigns a unique long
 * id to the update whenever an update method is called. The class is designed to be
 * reused from a pool of instances.
 */
public final class Update
{
  public Update()
  {
    active = false;
    mementos = new ArrayList<IMemento>( 3);
    setAttributeMemento = new SetAttributeMemento();
    removeAttributeMemento = new RemoveAttributeMemento();
    addChildMemento = new AddChildMemento();
    removeChildMemento = new RemoveChildMemento();
    moveChildMemento = new MoveChildMemento();
    setParentMemento = new SetParentMemento();
    reverted = false;
  }

  /**
   * Set the active state of the update. An update is active until all of its listeners have been notified.
   * @param active The active state.
   */
  public void setActive( boolean active)
  {
    this.active = active;
  }
  
  /**
   * @return Returns the active state.
   */
  public boolean isActive()
  {
    return active;
  }
  
  /**
   * Clear the list of mementos.
   */
  public void clear()
  {
    for( int i=0; i<mementos.size(); i++) mementos.get( i).clear();
    mementos.clear();
    reverted = false;
  }
  
  /**
   * Create mementos for an attribute set.
   * @param object The object whose attribute is being set.
   * @param attrName The name of the attribute.
   * @param newValue The new value.
   * @param oldValue The old value.
   */
  public void setAttribute( IModelObject object, String attrName, Object newValue, Object oldValue)
  {
    setAttributeMemento.object = object;
    setAttributeMemento.attrName = attrName;
    setAttributeMemento.newValue = newValue;
    setAttributeMemento.oldValue = oldValue;
    mementos.add( setAttributeMemento);
  }
  
  /**
   * Create mementos for an attribute remove.
   * @param object The object whose attribute is being removed.
   * @param attrName The name of the attribute.
   * @param oldValue The old value.
   */
  public void removeAttribute( IModelObject object, String attrName, Object oldValue)
  {
    removeAttributeMemento.object = object;
    removeAttributeMemento.attrName = attrName;
    removeAttributeMemento.oldValue = oldValue;
    mementos.add( removeAttributeMemento);
  }
  
  /**
   * Create mementos for a child add.
   * @param parent The parent.
   * @param child The child.
   * @param index The index.
   */
  public void addChild( IModelObject parent, IModelObject child, int index)
  {
    addChildMemento.parent = parent;
    addChildMemento.child = child;
    addChildMemento.index = index;
    mementos.add( addChildMemento);
    
    removeChildMemento.parent = child.getParent();
    if ( removeChildMemento.parent != null)
    {
      List<IModelObject> children = removeChildMemento.parent.getChildren();
      removeChildMemento.child = child;
      removeChildMemento.index = children.indexOf( child);
    }
    mementos.add( removeChildMemento);
    
    setParentMemento.child = child;
    setParentMemento.newParent = parent;
    setParentMemento.oldParent = child.getParent();
    mementos.add( setParentMemento);
  }
  
  /**
   * Create mementos for a child remove.
   * @param parent The parent.
   * @param child The child.
   * @param index The index.
   */
  public void removeChild( IModelObject parent, IModelObject child, int index)
  {
    removeChildMemento.parent = parent;
    removeChildMemento.child = child;
    removeChildMemento.index = index;
    mementos.add( removeChildMemento);
    
    setParentMemento.child = child;
    setParentMemento.newParent = null;
    setParentMemento.oldParent = child.getParent();
    mementos.add( setParentMemento);
  }
  
  /**
   * Create mementos for a child moved.
   * @param parent The parent.
   * @param child The child.
   * @param oldIndex The old index.
   * @param newIndex The new index.
   */
  public void moveChild( IModelObject parent, IModelObject child, int oldIndex, int newIndex)
  {
    moveChildMemento.parent = parent;
    moveChildMemento.child = child;
    moveChildMemento.oldIndex = oldIndex;
    moveChildMemento.newIndex = newIndex;
    mementos.add( moveChildMemento);
  }
  
  /**
   * Create a memento for a context variable.
   * @param scope The scope of the variable.
   * @param varName The name of the variable.
   * @param value The new value.
   * @param oldValue The old value.
   */
  public void setVariable( IVariableScope scope, String varName, Object value, Object oldValue)
  {
    VariableMemento variableMemento = new VariableMemento();
    variableMemento.scope = scope;
    variableMemento.varName = varName;
    variableMemento.newValue = value;
    variableMemento.oldValue = oldValue;
    mementos.add( variableMemento);
  }
  
  /**
   * Returns the number of mementos in the update.
   * @return Returns the number of mementos in the update.
   */
  public int size()
  {
    return mementos.size();
  }

  /**
   * Revert this update.
   */
  public void revert()
  {
    if ( !reverted)
    {
      reverted = true;
      for( IMemento memento: mementos) 
      {
        try
        {
          memento.revert();
        }
        catch( Exception e)
        {
          System.err.println( modificationWarningMessage);
          log.exception( e);
        }
      }
    }
  }

  /**
   * Restore this update.
   */
  public void restore()
  {
    if ( reverted)
    {
      reverted = false;
      for( IMemento memento: mementos) 
      {
        try
        {
          memento.restore();
        }
        catch( Exception e)
        {
          System.err.println( modificationWarningMessage);
          log.exception( e);
        }
      }
    }
  }
  
  /**
   * Returns true if this Update is currently reverted.
   * @return Returns true if this Update is currently reverted.
   */
  public boolean isReverted()
  {
    return reverted;
  }

  /**
   * Returns an IChangeSet which will store changes to an object which cannot be processed
   * until after the current update to the object is completed.  The changes in the deferred
   * change set will be processed when the Update ends.
   * @return Returns the IChangeSet used to store deferred changes.
   */
  public IChangeSet getDeferredChangeSet()
  {
    if ( deferred == null) deferred = new ChangeSet();
    return deferred;
  }
  
  /**
   * Process the changes in the deferred IChangeSet.
   */
  public void processDeferred()
  {
    IChangeSet changeSet = deferred;
    if ( deferred != null)
    {
      deferred = null;
      changeSet.applyChanges();
    }
  }

  private static Log log = Log.getLog( "org.xmodel");
  
  private final static String modificationWarningMessage =
    "Warning: Two or more listeners have updated the object which triggered their\n" +
    "  notification. The prior state of the model could not be restored and some\n" +
    "  listeners may not have triggered appropriately. This problem is an indication\n" +
    "  of a design problem and can always be avoided.";

  private boolean active;
  private List<IMemento> mementos;
  private SetAttributeMemento setAttributeMemento;
  private RemoveAttributeMemento removeAttributeMemento;
  private AddChildMemento addChildMemento;
  private RemoveChildMemento removeChildMemento;
  private MoveChildMemento moveChildMemento;
  private SetParentMemento setParentMemento;
  private boolean reverted;
  private IChangeSet deferred;
}  
