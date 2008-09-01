/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel;

import java.util.ArrayList;
import java.util.List;
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
    mementos = new ArrayList<IMemento>( 3);
    setAttributeMemento = new SetAttributeMemento();
    removeAttributeMemento = new RemoveAttributeMemento();
    addChildMemento = new AddChildMemento();
    removeChildMemento = new RemoveChildMemento();
    setParentMemento = new SetParentMemento();
    reverted = false;
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
    removeChildMemento.parent = parent;
    removeChildMemento.child = child;
    removeChildMemento.index = oldIndex;
    mementos.add( removeChildMemento);
    
    addChildMemento.parent = parent;
    addChildMemento.child = child;
    addChildMemento.index = newIndex;
    mementos.add( addChildMemento);
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
   * Set the id of this update.
   * @param id The id.
   */
  public void setId( int id)
  {
    this.id = id;
  }
  
  /**
   * Returns the id for this update.
   * @return Returns the id for this update.
   */
  public int getId()
  {
    return id;
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
          e.printStackTrace( System.err);
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
          e.printStackTrace( System.err);
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

  private final static String modificationWarningMessage =
    "Warning: Two or more listeners have updated the object which triggered their\n" +
    "  notification. The prior state of the model could not be restored and some\n" +
    "  listeners may not have triggered appropriately. This problem is an indication\n" +
    "  of a design problem and can always be avoided.";
  
  private int id;
  private List<IMemento> mementos;
  private SetAttributeMemento setAttributeMemento;
  private RemoveAttributeMemento removeAttributeMemento;
  private AddChildMemento addChildMemento;
  private RemoveChildMemento removeChildMemento;
  private SetParentMemento setParentMemento;
  private boolean reverted;
  private IChangeSet deferred;
}  
