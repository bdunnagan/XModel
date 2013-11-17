/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ModelObject.java
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.xmodel.external.CachingException;
import org.xmodel.external.ICachingPolicy;
import org.xmodel.external.ITransaction;
import org.xmodel.log.Log;
import org.xmodel.memento.AddChildMemento;
import org.xmodel.memento.IMemento;
import org.xmodel.memento.MoveChildMemento;
import org.xmodel.memento.RemoveAttributeMemento;
import org.xmodel.memento.RemoveChildMemento;
import org.xmodel.memento.SetAttributeMemento;
import org.xmodel.memento.SetParentMemento;
import org.xmodel.storage.IStorageClass;
import org.xmodel.storage.SmallDataStorageClass;
import org.xmodel.storage.ValueStorageClass;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.AttributeNode;

/**
 * An reference implementation of IModelObject.
 * <p>
 * During listener notification, changes made to the object performing the notification must be deferred
 * until all listeners have been notified.  This guarantees that all listeners are notified of all changes
 * regardless of the order in which the listeners are iterated.
 * <p>
 * Objects which are created without a name will return a name which is guaranteed to be unique across
 * multiple invocations of the application.  The name consists of a time-stamp, a secure random number
 * and the Java system hash code for the object.  The generated name is not stored in the attribute table.
 * <p>
 * <b>Warning #1: The lists returned by this implementation are modifiable, but they should never be modified.
 * <b>Warning #2: Fragments containing this instance may only ever be accessed by a single thread.
 */
public class ModelObject implements IModelObject
{
  /**
   * Create a ModelObject with the specified type.
   * @param type The type.
   */
  public ModelObject( String type)
  {
    this( new ValueStorageClass(), type);
  }
  
  /**
   * Create a ModelObject with the specified type and id.
   * @param type The type.
   * @param id The id.
   */
  public ModelObject( String type, String id)
  {
    this( new SmallDataStorageClass(), type);
    setAttributeImpl( "id", id);
  }
  
  /**
   * Create a ModelObject with the specified initial IStorageClass and type.
   * @param storageClass The initial storage class.
   * @param type The type.
   */
  public ModelObject( IStorageClass storageClass, String type)
  {
    this.type = type.intern();
    this.storageClass = storageClass;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getType()
   */
  public String getType()
  {
    return type;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#isType(java.lang.String)
   */
  public boolean isType( String type)
  {
    return this.type.equals( type);
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.IExternalReference#setCachingPolicy(org.xmodel.external.ICachingPolicy)
   */
  public void setCachingPolicy( ICachingPolicy newCachingPolicy)
  {
    storageClass = storageClass.getCachingPolicyStorageClass();
    storageClass.setCachingPolicy( newCachingPolicy);
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.IExternalReference#setDirty(boolean)
   */
  public void setDirty( boolean dirty)
  {
    storageClass = storageClass.getCachingPolicyStorageClass();
    
    // 050109: added this back during xidget tree development
    boolean wasDirty = storageClass.getDirty();
    storageClass.setDirty( dirty);
    if ( wasDirty != dirty) 
    {
      notifyDirty( dirty);

      if ( dirty)
      {
        // resync immediately if reference has listeners
        ModelListenerList listeners = getModelListeners();
        if ( listeners != null && listeners.count() > 0) getChildren();
      }
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.IExternalReference#isDirty()
   */
  public boolean isDirty()
  {
    return storageClass.getDirty();
  }

  /* (non-Javadoc)
   * @see org.xmodel.reference.IExternalObject#getCachingPolicy()
   */
  public ICachingPolicy getCachingPolicy()
  {
    return storageClass.getCachingPolicy();
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.IExternalReference#sync()
   */
  public void sync() throws CachingException
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.IExternalReference#transaction()
   */
  @Override
  public ITransaction transaction()
  {
    ICachingPolicy cachingPolicy = getCachingPolicy();
    if ( cachingPolicy == null) throw new CachingException( "No caching policy for this entity: "+this);
    return cachingPolicy.transaction();
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.IExternalReference#clearCache()
   */
  public void clearCache() throws CachingException
  {
    throw new UnsupportedOperationException();
  }
  
  /**
   * Notify listeners that the dirty state of a reference has changed.
   * @param reference The reference.
   * @param dirty The new dirty state.
   */
  protected void notifyDirty( boolean dirty)
  {
    ModelListenerList listeners = getModelListeners();
    if ( listeners != null) listeners.notifyDirty( this, dirty);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#setAttribute(java.lang.String)
   */
  public Object setAttribute( String attrName)
  {
    return setAttribute( attrName, "");
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#setAttribute(java.lang.String, java.lang.Object)
   */
  public Object setAttribute( String attrName, Object attrValue)
  {
    notifyAccessAttributes( attrName, true);
    
    storageClass = storageClass.getAttributeStorageClass( attrName);
    
    Object oldValue = getAttribute( attrName);
    if ( oldValue != null && attrValue != null && oldValue.equals( attrValue)) return oldValue;

    if ( parent != null && attrName.equals( "id"))
      log.warnf( "LATE-ID: %s, id=%s", this, attrValue);
    
    if ( attrValue == null) 
    {
      removeAttribute( attrName);
      return oldValue;
    }
    
    IModel model = GlobalSettings.getInstance().getModel();
    IChangeSet transaction = model.isFrozen( this);
    if ( transaction != null)
    {
      transaction.setAttribute( this, attrName, attrValue);
      return oldValue;
    }
    else
    {
      Update update = model.startUpdate();
      update.setAttribute( this, attrName, attrValue, oldValue);
      setAttributeImpl( attrName, attrValue);
      
      // lock state and notify
      model.freeze( this);
      notifyChange( attrName, attrValue, oldValue);
      
      // unlock state and end update
      model.unfreeze( this);
      model.endUpdate();
      return oldValue;
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getAttribute(java.lang.String)
   */
  public Object getAttribute( String attrName)
  {
    notifyAccessAttributes( attrName, false);
    return storageClass.getAttribute( attrName);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getAttributeNode(java.lang.String)
   */
  public IModelObject getAttributeNode( String attrName)
  {
    Object value = storageClass.getAttribute( attrName);
    return (value != null)? new AttributeNode( attrName, this): null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getAllAttributes()
   */
  public Collection<String> getAttributeNames()
  {
    notifyAccessAttributes( null, false);
    return storageClass.getAttributeNames();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#removeAttribute(java.lang.String)
   */
  public Object removeAttribute( String attrName)
  {
    notifyAccessAttributes( attrName, true);
    
    Object oldValue = getAttribute( attrName);
    if ( oldValue == null) return null;
    
    IModel model = GlobalSettings.getInstance().getModel();
    IChangeSet transaction = model.isFrozen( this);
    if ( transaction != null)
    {
      transaction.removeAttribute( this, attrName);
      return oldValue;
    }
    else
    {
      Update update = model.startUpdate();
      update.removeAttribute( this, attrName, oldValue);
      removeAttributeImpl( attrName);
      
      // lock state and notify
      model.freeze( this);
      notifyClear( attrName, oldValue);
      
      // unlock state and end update
      model.unfreeze( this);
      model.endUpdate();
      return oldValue;
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#setValue(java.lang.Object)
   */
  public Object setValue( Object value)
  {
    return setAttribute( "", value);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getValue()
   */
  public Object getValue()
  {
    return getAttribute( "");
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getChildValue(java.lang.String)
   */
  public Object getChildValue( String type)
  {
    IModelObject child = getFirstChild( type);
    if ( child != null) return child.getValue();
    return null;
  }

  /**
   * Set the specified attribute in the internal object data and return old value.
   * @param attrName The attribute name.
   * @param attrValue The attribute value.
   * @return Returns the old value of the attribute.
   */
  protected Object setAttributeImpl( String attrName, Object attrValue)
  {
    storageClass = storageClass.getAttributeStorageClass( attrName);
    return storageClass.setAttribute( attrName, attrValue);
  }

  /**
   * Remove the specified attribute from the internal object data and return its value.
   * @param attrName The attribute.
   * @return Returns the value of the attribute.
   */
  protected Object removeAttributeImpl( String attrName)
  {
    return storageClass.setAttribute( attrName, null);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#addChild(org.xmodel.IModelObject)
   */
  public void addChild( IModelObject child)
  {
    storageClass = storageClass.getChildrenStorageClass();
    addChild( child, -1);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#addChild(org.xmodel.IModelObject, int)
   */
  public void addChild( IModelObject child, int index)
  {
    if ( child == this) throw new IllegalArgumentException();
    
    if ( index < 0) index += getChildren().size() + 1;
    
    IModel model = GlobalSettings.getInstance().getModel();
    IChangeSet transaction = model.isFrozen( this);
    if ( transaction != null)
    {
      transaction.addChild( this, child, index);
    }
    else
    {
      notifyAccessChildren( true);
      
      IModelObject oldParent = child.getParent();
      if ( oldParent == this)
      {
        // create mementos
        int oldIndex = getChildren().indexOf( child);
        
        Update update = model.startUpdate();
        update.moveChild( this, child, oldIndex, index);

        // reposition child
        if ( oldIndex < index) index--;
        removeChildImpl( oldIndex);
        addChildImpl( child, index);
        
        // lock state and notify
        model.freeze( this); 
        model.freeze( child);
        notifyRemoveChild( child, oldIndex);
        notifyAddChild( child, index);
        
        // unlock state and end update
        model.unfreeze( this); 
        model.unfreeze( child);
        model.endUpdate();
      }
      else
      {
        // create mementos
        Update update = model.startUpdate();
        update.addChild( this, child, index);
        
        // update parent of child
        child.internal_setParent( this);
        
        // remove child from old parent
        int oldIndex = -1;
        if ( oldParent != null) 
        {
          oldIndex = oldParent.getChildren().indexOf( child);
          oldParent.internal_removeChild( oldIndex);
        }
        
        // add child to new parent (this)
        addChildImpl( child, index);
        
        // lock state and notify
        model.freeze( this); 
        model.freeze( oldParent); 
        model.freeze( child);
        
        child.internal_notifyParent( this, oldParent);
        if ( oldParent != null) oldParent.internal_notifyRemoveChild( child, oldIndex);
        notifyAddChild( child, index);
        
        // unlock state and end update
        model.unfreeze( this); 
        model.unfreeze( oldParent); 
        model.unfreeze( child);
        model.endUpdate();
      }
    }
  }

  /**
   * Add the specified child to the internal object data.
   * @param child The child.
   * @param index The index where the child will be located.
   */
  protected void addChildImpl( IModelObject child, int index)
  {
    storageClass = storageClass.getChildrenStorageClass();
    if ( index == -1) index = storageClass.getChildren().size();
    storageClass.getChildren().add( index, child);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#removeChild(int)
   */
  public IModelObject removeChild( int index)
  {
    IModel model = GlobalSettings.getInstance().getModel();
    IChangeSet transaction = model.isFrozen( this);
    if ( transaction != null)
    {
      // bail if no children
      if ( storageClass.getChildren() == null) return null;
      
      // create change record
      IModelObject child = getChild( index);
      transaction.removeChild( this, child, index);
      return child;
    }
    else
    {
      notifyAccessChildren( true);
      
      // bail if no children
      if ( storageClass.getChildren() == null) return null;
      
      IModelObject child = storageClass.getChildren().get( index);
      if ( child != null)
      {
        Update update = model.startUpdate();
        update.removeChild( this, child, index);
        
        removeChildImpl( index);
        child.internal_setParent( null);
        
        // lock state and notify
        model.freeze( this);
        model.freeze( child);
        notifyRemoveChild( child, index);
        
        // unlock state and end update
        model.unfreeze( this);
        model.unfreeze( child);
        model.endUpdate();
      }
      return child;
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#removeChild(org.xmodel.IModelObject)
   */
  public void removeChild( IModelObject child)
  {
    IModel model = GlobalSettings.getInstance().getModel();
    IChangeSet transaction = model.isFrozen( this);
    if ( transaction != null)
    {
      transaction.removeChild( this, child);
    }
    else
    {
      notifyAccessChildren( true);
      
      // bail if no children
      if ( storageClass.getChildren() == null) return;
      
      int index = storageClass.getChildren().indexOf( child);
      if ( index >= 0)
      {
        Update update = model.startUpdate();
        update.removeChild( this, child, index);
          
        removeChildImpl( index);
        child.internal_setParent( null);

        // lock state and notify
        model.freeze( this);
        model.freeze( child);
        notifyRemoveChild( child, index);
        
        // unlock state and end update
        model.unfreeze( this);
        model.unfreeze( child);
        model.endUpdate();
      }
    }
  }

  /**
   * Remove the specified child from the internal object data and return it.
   * @param index The index of the child.
   * @return Returns the child which was removed.
   */
  protected IModelObject removeChildImpl( int index)
  {
    List<IModelObject> children = storageClass.getChildren();
    if ( children == null) return null;
    return children.remove( index);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#removeChildren()
   */
  public void removeChildren()
  {
    List<IModelObject> children = storageClass.getChildren();
    while( children != null && children.size() > 0)
      removeChild( children.size() - 1);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#removeChildren(java.lang.String)
   */
  public void removeChildren( String type)
  {
    List<IModelObject> children = storageClass.getChildren();
    if ( children != null)
    {
      List<IModelObject> list = new ArrayList<IModelObject>( children.size());
      list.addAll( (List<IModelObject>)children);
      for ( IModelObject child: list)
        if ( child.isType( type))
          removeChild( child);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#removeFromParent()
   */
  public void removeFromParent()
  {
    IModelObject parent = getParent();
    if ( parent != null) parent.removeChild( this);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getChild(int)
   */
  public IModelObject getChild( int index)
  {
    notifyAccessChildren( false);
    
    List<IModelObject> children = storageClass.getChildren();
    
    // bail if no children
    if ( children == null) return null;
    
    // bail if index too large
    if ( index >= children.size()) return null;
    
    // get child
    return children.get( index);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getFirstChild(java.lang.String)
   */
  public IModelObject getFirstChild( String type)
  {
    notifyAccessChildren( false);
    
    List<IModelObject> children = storageClass.getChildren();
    if ( children != null)
    {
      for( IModelObject child: children)
        if ( child.isType( type))
          return child;
    }
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getChild(java.lang.String, java.lang.Object)
   */
  public IModelObject getChild( String type, Object id)
  {
    notifyAccessChildren( false);
    
    List<IModelObject> children = storageClass.getChildren();
    if ( children != null)
    {
      for( IModelObject child: children)
        if ( child.isType( type) && child.getAttribute( "id").equals( id))
          return child;
    }
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getCreateChild(java.lang.String)
   */
  public IModelObject getCreateChild( String type)
  {
    IModelObject child = getFirstChild( type);
    if ( child == null)
    {
      child = createObject( type);
      addChild( child);
    }
    return child;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getCreateChild(java.lang.String, java.lang.Object)
   */
  public IModelObject getCreateChild( String type, Object id)
  {
    IModelObject child = getChild( type, id);
    if ( child == null) 
    {
      child = createObject( type);
      child.setAttribute( "id", id);
      addChild( child);
    }
    return child;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getChildren(java.lang.String, java.lang.Object)
   */
  public List<IModelObject> getChildren( String type, Object id)
  {
    notifyAccessChildren( false);
    
    List<IModelObject> result = new ArrayList<IModelObject>( 1);
    List<IModelObject> children = storageClass.getChildren();
    if ( children != null)
    {
      for( IModelObject child: children)
        if ( child.isType( type) && child.getAttribute( "id").equals( id))
          result.add( child);
    }
    return result;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getChildren()
   */
  public List<IModelObject> getChildren()
  {
    notifyAccessChildren( false);
    
    List<IModelObject> children = storageClass.getChildren();
    List<IModelObject> result = children;
    if ( children == null) result = Collections.emptyList();
    
    return result;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getChildren(java.lang.String)
   */
  public List<IModelObject> getChildren( String type)
  {
    notifyAccessChildren( false);
    
    List<IModelObject> children = storageClass.getChildren();
    if ( children != null)
    {
      List<IModelObject> result = new ArrayList<IModelObject>( children.size());
      for( IModelObject child: children)
        if ( child.isType( type))
          result.add( child);
      return result;
    }
    
    return Collections.emptyList();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getTypesOfChildren()
   */
  public Set<String> getTypesOfChildren()
  {
    notifyAccessChildren( false);
    
    HashSet<String> set = new HashSet<String>();
    List<IModelObject> children = storageClass.getChildren();
    if ( children != null)
    {
      for( IModelObject child: children)
        set.add( child.getType());
    }
    return set;   
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getNumberOfChildren()
   */
  public int getNumberOfChildren()
  {
    notifyAccessChildren( false);
    List<IModelObject> children = storageClass.getChildren();
    return (children == null)? 0: children.size();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getNumberOfChildren(java.lang.String)
   */
  public int getNumberOfChildren( String type)
  {
    notifyAccessChildren( false);
    return getChildren( type).size();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#internal_notifyParent(org.xmodel.IModelObject, org.xmodel.IModelObject)
   */
  public void internal_notifyParent( IModelObject newParent, IModelObject oldParent)
  {
    notifyParent( newParent, oldParent);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#internal_notifyAdd(org.xmodel.IModelObject, int)
   */
  public void internal_notifyAddChild( IModelObject child, int index)
  {
    notifyAddChild( child, index);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#internal_notifyRemove(org.xmodel.IModelObject, int)
   */
  public void internal_notifyRemoveChild( IModelObject child, int index)
  {
    notifyRemoveChild( child, index);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#internal_addChild(org.xmodel.IModelObject, int)
   */
  public void internal_addChild( IModelObject child, int index)
  {
    addChildImpl( child, index);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#internal_removeChild(int)
   */
  public IModelObject internal_removeChild( int index)
  {
    return removeChildImpl( index);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#internal_setParent(org.xmodel.IModelObject)
   */
  public IModelObject internal_setParent( IModelObject newParent)
  {
    IModelObject oldParent = parent;
    parent = newParent;
    return oldParent;
  }

  /**
   * Set the parent in the internal object data and return old parent.
   * @param newParent The parent.
   * @return Returns the old parent.
   */
  protected IModelObject setParentImpl( IModelObject newParent)
  {
    IModelObject oldParent = parent;
    parent = newParent;
    return oldParent;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getParent()
   */
  public IModelObject getParent()
  {
    return parent;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getAncestor(java.lang.String)
   */
  public IModelObject getAncestor( String type)
  {
    IModelObject ancestor = getParent();
    while( ancestor != null) 
    {
      if ( ancestor.isType( type)) return ancestor;
      ancestor = ancestor.getParent();
    }
    return ancestor;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getRoot()
   */
  public IModelObject getRoot()
  {
    IModelObject child = this;
    IModelObject parent = child.getParent();
    while( parent != null) 
    {
      child = parent;
      parent = parent.getParent();
    }
    return child;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#addListener(org.xmodel.IModelListener)
   */
  public void addModelListener( IModelListener listener)
  {
    storageClass = storageClass.getModelListenersStorageClass();
    storageClass.getModelListeners().addListener( listener);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#removeListener(org.xmodel.IModelListener)
   */
  public void removeModelListener( IModelListener listener)
  {
    ModelListenerList listeners = storageClass.getModelListeners();
    if ( listeners == null) return;
    listeners.removeListener( listener);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getModelListeners()
   */
  public ModelListenerList getModelListeners()
  {
    return storageClass.getModelListeners();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getPathListeners()
   */
  public PathListenerList getPathListeners()
  {
    storageClass = storageClass.getPathListenersStorageClass();
    return storageClass.getPathListeners();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#cloneObject()
   */
  public IModelObject cloneObject()
  {
    IModelObject clone = createObject( getType());
    ModelAlgorithms.copyAttributes( this, clone);
    return clone;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#cloneTree()
   */
  public IModelObject cloneTree()
  {
    return ModelAlgorithms.cloneTree( this);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#createObject(java.lang.String)
   */
  public IModelObject createObject( String type)
  {
    return new ModelObject( type);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getReferent()
   */
  public IModelObject getReferent()
  {
    return this;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#revertUpdate(org.xmodel.memento.IMemento)
   */
  public void revertUpdate( IMemento iMemento)
  {
    if ( iMemento instanceof SetAttributeMemento)
    {
      SetAttributeMemento memento = (SetAttributeMemento)iMemento;
      storageClass.setAttribute( memento.attrName, memento.oldValue);
    }
    else if ( iMemento instanceof SetParentMemento)
    {
      SetParentMemento memento = (SetParentMemento)iMemento;
      parent = memento.oldParent;
    }
    else if ( iMemento instanceof AddChildMemento)
    {
      AddChildMemento memento = (AddChildMemento)iMemento;
      storageClass.getChildren().remove( memento.index);
    }
    else if ( iMemento instanceof RemoveChildMemento)
    {
      RemoveChildMemento memento = (RemoveChildMemento)iMemento;
      storageClass.getChildren().add( memento.index, memento.child);
    }
    else if ( iMemento instanceof MoveChildMemento)
    {
      MoveChildMemento memento = (MoveChildMemento)iMemento;
      List<IModelObject> children = storageClass.getChildren();
      children.remove( (memento.newIndex > memento.oldIndex)? (memento.newIndex - 1): memento.newIndex);
      children.add( memento.oldIndex, memento.child);
    }
    else
    {
      RemoveAttributeMemento memento = (RemoveAttributeMemento)iMemento;
      storageClass.setAttribute( memento.attrName, memento.oldValue);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#restoreUpdate(org.xmodel.memento.IMemento)
   */
  public void restoreUpdate( IMemento iMemento)
  {
    if ( iMemento instanceof SetAttributeMemento)
    {
      SetAttributeMemento memento = (SetAttributeMemento)iMemento;
      storageClass.setAttribute( memento.attrName, memento.newValue);
    }
    else if ( iMemento instanceof SetParentMemento)
    {
      SetParentMemento memento = (SetParentMemento)iMemento;
      parent = memento.newParent;
    }
    else if ( iMemento instanceof AddChildMemento)
    {
      AddChildMemento memento = (AddChildMemento)iMemento;
      storageClass.getChildren().add( memento.index, memento.child);
    }
    else if ( iMemento instanceof RemoveChildMemento)
    {
      RemoveChildMemento memento = (RemoveChildMemento)iMemento;
      storageClass.getChildren().remove( memento.index);
    }
    else if ( iMemento instanceof MoveChildMemento)
    {
      MoveChildMemento memento = (MoveChildMemento)iMemento;
      List<IModelObject> children = storageClass.getChildren();
      children.remove( memento.oldIndex);
      children.add( (memento.newIndex > memento.oldIndex)? (memento.newIndex - 1): memento.newIndex, memento.child);
    }
    else
    {
      RemoveAttributeMemento memento = (RemoveAttributeMemento)iMemento;
      storageClass.setAttribute( memento.attrName, null);
    }
  }

  /**
   * Called just before a request that accesses the attributes of this object is fulfilled.
   * @param name The name of the attribute, or null if all attributes are being accessed.
   * @param write True if write access.
   */
  protected void notifyAccessAttributes( String name, boolean write)
  {
  }
  
  /**
   * Called just before a request that accesses the children of this object is fulfilled.
   * @param write True if write access.
   */
  protected void notifyAccessChildren( boolean write)
  {
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append( '<');
    builder.append( getType());

    // value
    Object text = storageClass.getAttribute( "");
    if ( text == null) text = "";
    
    // attributes
    for( String attrName: storageClass.getAttributeNames())
    {
      if ( attrName.length() == 0) continue;
      builder.append( ' '); builder.append( attrName); builder.append( "='"); builder.append( storageClass.getAttribute( attrName));
      builder.append( '\'');
    }
    
    // children
    List<IModelObject> children = storageClass.getChildren();
    if ( children != null && children.size() > 0)
    {
      if ( text == null || text.equals( ""))
      {
        builder.append( ">...");
      }
      else
      {
        builder.append( '>');
        builder.append( text.toString().trim());
        builder.append( "...");
      }
    }
    else if ( text == null || text.equals( ""))
    {
      builder.append( "/>");
    }
    else
    {
      builder.append( '>');
      builder.append( text);
      builder.append( "</");
      builder.append( getType());
      builder.append( '>');
    }

    return builder.toString();
  }
  
  /**
   * Returns the xml representation of the subtree.
   * @return Returns the xml representation of the subtree.
   */
  public String toXml()
  {
    IModel model = GlobalSettings.getInstance().getModel();
    boolean syncLock = model.getSyncLock();
    try
    {
      model.setSyncLock( true);
      return XmlIO.toString( this);
    }
    finally
    {
      model.setSyncLock( syncLock);
    }
  }
  
  /**
   * Do parent change notification to listeners. An IChangeSet is installed before doing the notification
   * so that any changes made to the model from within an IModelListener context will be deferred until all
   * listeners have received notification. The records are processed at the end of this method. Notification
   * is performed within a try/catch block which catches all exceptions of type Exception and logged to
   * stderr.
   * @param newParent The new parent.
   * @param oldParent The old parent.
   */
  private void notifyParent( IModelObject newParent, IModelObject oldParent)
  {
    ModelListenerList listeners = storageClass.getModelListeners();
    if ( listeners != null) listeners.notifyParent( this, newParent, oldParent);
  }
  
  /**
   * Do child added notification to listeners. An IChangeSet is installed before doing the notification
   * so that any changes made to the model from within an IModelListener context will be deferred until all
   * listeners have received notification. The records are processed at the end of this method. Notification
   * is performed within a try/catch block which catches all exceptions of type Exception and logged to
   * stderr.
   * @param child The child which was added.
   * @param index The index at which the child was added.
   */
  private void notifyAddChild( IModelObject child, int index)
  {
    ModelListenerList listeners = storageClass.getModelListeners();
    if ( listeners != null) listeners.notifyAddChild( this, child, index);
  }
  
  /**
   * Do child removed notification to listeners. An IChangeSet is installed before doing the notification
   * so that any changes made to the model from within an IModelListener context will be deferred until all
   * listeners have received notification. The records are processed at the end of this method. Notification
   * is performed within a try/catch block which catches all exceptions of type Exception and logged to
   * stderr.
   * @param child The child which was removed.
   * @param index The index of the child that was removed.
   */
  private void notifyRemoveChild( IModelObject child, int index)
  {
    ModelListenerList listeners = storageClass.getModelListeners();
    if ( listeners != null) listeners.notifyRemoveChild( this, child, index);
  }
  
  /**
   * Do attribute change notification to listeners. An IChangeSet is installed before doing the notification
   * so that any changes made to the model from within an IModelListener context will be deferred until all
   * listeners have received notification. The records are processed at the end of this method. Notification
   * is performed within a try/catch block which catches all exceptions of type Exception and logged to
   * stderr.
   * @param attrName The name of the attribute.
   * @param newValue The new value of the attribute.
   * @param oldValue The old value of the attribute.
   */
  private void notifyChange( String attrName, Object newValue, Object oldValue)
  {
    ModelListenerList listeners = storageClass.getModelListeners();
    if ( listeners != null) listeners.notifyChange( this, attrName, newValue, oldValue);
  }
  
  /**
   * Do attribute clear notification to listeners. An IChangeSet is installed before doing the notification
   * so that any changes made to the model from within an IModelListener context will be deferred until all
   * listeners have received notification. The records are processed at the end of this method. Notification
   * is performed within a try/catch block which catches all exceptions of type Exception and logged to
   * stderr.
   * @param attrName The name of the attribute.
   * @param oldValue The old value of the attribute.
   */
  private void notifyClear( String attrName, Object oldValue)
  {
    ModelListenerList listeners = storageClass.getModelListeners();
    if ( listeners != null) listeners.notifyClear( this, attrName, oldValue);
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals( Object object)
  {
    if ( object instanceof Reference) return object.equals( this);
    return super.equals( object);
  }
  
  protected IModelObject parent;
  protected String type;
  protected IStorageClass storageClass;
  
  private static Log log = Log.getLog( ModelObject.class);
}
