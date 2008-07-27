/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel;

import java.util.*;

import dunnagan.bob.xmodel.listeners.ClimbingListener;
import dunnagan.bob.xmodel.memento.*;
import dunnagan.bob.xmodel.xml.XmlIO;
import dunnagan.bob.xmodel.xpath.AttributeNode;

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
 * <b>Warning: The lists returned by this implementation are modifiable, but they should never be modified.
 */
public class ModelObject implements IModelObject
{
  /**
   * Create a ModelObject with the specified type.
   * @param type The type.
   */
  public ModelObject( String type)
  {
    this.type = type.intern();
  }
  
  /**
   * Create a ModelObject with the specified type and id.
   * @param type The type.
   * @param id The id.
   */
  public ModelObject( String type, String id)
  {
    this( type);
    setAttributeImpl( "id", id);
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getModel()
   */
  public IModel getModel()
  {
    if ( model == null) model = ModelRegistry.getInstance().getModel();
    return model;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#setName(java.lang.String)
   */
  public void setName( String name)
  {
    writeAttributeAccess( "id");
    setAttribute( "id", name);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getName()
   */
  public String getName()
  {
    readAttributeAccess( "id");
    return Xlate.get( this, "id", "");
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#setID(java.lang.String)
   */
  public void setID( String id)
  {
    writeAttributeAccess( "id");
    setAttribute( "id", id);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getID()
   */
  public String getID()
  {
    readAttributeAccess( "id");
    return Xlate.get( this, "id", "");
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getType()
   */
  public String getType()
  {
    return type;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#isType(java.lang.String)
   */
  public boolean isType( String type)
  {
    return this.type.equals( type);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#isDirty()
   */
  public boolean isDirty()
  {
    return false;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#setAttribute(java.lang.String)
   */
  public Object setAttribute( String attrName)
  {
    return setAttribute( attrName, "");
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#setAttribute(java.lang.String, java.lang.Object)
   */
  public Object setAttribute( String attrName, Object attrValue)
  {
    writeAttributeAccess( attrName);
    
    Object oldValue = getAttribute( attrName);
    if ( oldValue != null && attrValue != null && oldValue.equals( attrValue)) return oldValue;

    // DEBUG
    if ( parent != null && attrName.equals( "id"))
      System.out.println( "LATE-ID: "+this+", id="+attrValue);
    // DEBUG
    
    if ( attrValue == null) 
    {
      removeAttribute( attrName);
      return oldValue;
    }
    
    IChangeSet transaction = getModel().isLocked( this);
    if ( transaction != null)
    {
      transaction.setAttribute( this, attrName, attrValue);
      return oldValue;
    }
    else
    {
      IModel model = getModel();
      if ( model != null)
      {
        Update update = model.startUpdate();
        update.setAttribute( this, attrName, attrValue, oldValue);
      }
      setAttributeImpl( attrName, attrValue);
      
      // lock state and notify
      model.lock( this);
      notifyChange( attrName, attrValue, oldValue);
      
      // unlock state and end update
      model.unlock( this);
      if ( model != null) model.endUpdate();
      return oldValue;
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getAttribute(java.lang.String)
   */
  public Object getAttribute( String attrName)
  {
    readAttributeAccess( attrName);
    
    if ( attributes == null) return null;
    return attributes.get( attrName);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getAttributeNode(java.lang.String)
   */
  public IModelObject getAttributeNode( String attrName)
  {
    if ( attributes != null && !attributes.containsKey( attrName)) return null;
    return new AttributeNode( attrName, this);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getAllAttributes()
   */
  public Collection<String> getAttributeNames()
  {
    readAttributeAccess( null);
    
    if ( attributes == null) return Collections.emptyList();
    return attributes.keySet();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#removeAttribute(java.lang.String)
   */
  public Object removeAttribute( String attrName)
  {
    writeAttributeAccess( attrName);
    
    Object oldValue = getAttribute( attrName);
    if ( oldValue == null) return null;
    
    IChangeSet transaction = getModel().isLocked( this);
    if ( transaction != null)
    {
      transaction.removeAttribute( this, attrName);
      return oldValue;
    }
    else
    {
      IModel model = getModel();
      if ( model != null)
      {
        Update update = model.startUpdate();
        update.removeAttribute( this, attrName, oldValue);
      }
      removeAttributeImpl( attrName);
      
      // lock state and notify
      model.lock( this);
      notifyClear( attrName, oldValue);
      
      // unlock state and end update
      model.unlock( this);
      if ( model != null) model.endUpdate();
      return oldValue;
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#setValue(java.lang.Object)
   */
  public Object setValue( Object value)
  {
    return setAttribute( "", value);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getValue()
   */
  public Object getValue()
  {
    return getAttribute( "");
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getChildValue(java.lang.String)
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
    if ( attributes == null) attributes = new HashMap<String,Object>();
    return attributes.put( attrName.intern(), attrValue);
  }

  /**
   * Remove the specified attribute from the internal object data and return its value.
   * @param attrName The attribute.
   * @return Returns the value of the attribute.
   */
  protected Object removeAttributeImpl( String attrName)
  {
    if ( attributes == null) return null;
    return attributes.remove( attrName);
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#addChild(dunnagan.bob.xmodel.IModelObject)
   */
  public void addChild( IModelObject child)
  {
    int index = (children != null)? children.size(): 0;
    addChild( child, index);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#addChild(dunnagan.bob.xmodel.IModelObject, int)
   */
  public void addChild( IModelObject child, int index)
  {
    if ( child == this) throw new IllegalArgumentException();
    
    IChangeSet transaction = getModel().isLocked( this);
    if ( transaction != null)
    {
      transaction.addChild( this, child, index);
    }
    else
    {
      writeChildrenAccess();
      
      IModelObject oldParent = child.getParent();
      if ( oldParent == this)
      {
        // create mementos
        int oldIndex = getChildren().indexOf( child);
        IModel model = getModel();
        if ( model != null)
        {
          Update update = model.startUpdate();
          update.moveChild( this, child, oldIndex, index);
        }

        // reposition child
        if ( removeChildImpl( oldIndex) != null && oldIndex < index) index--;
        addChildImpl( child, index);
        
        // lock state and notify
        model.lock( this); 
        model.lock( child);
        notifyRemoveChild( child, oldIndex);
        notifyAddChild( child, index);
        
        // unlock state and end update
        model.unlock( this); 
        model.unlock( child);
        if ( model != null) model.endUpdate();
      }
      else
      {
        // create mementos
        IModel model = getModel();
        if ( model != null)
        {
          Update update = model.startUpdate();
          update.addChild( this, child, index);
        }
        
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
        model.lock( this); 
        model.lock( oldParent); 
        model.lock( child);
        
        child.internal_notifyParent( this, oldParent);
        if ( oldParent != null) oldParent.internal_notifyRemoveChild( child, oldIndex);
        notifyAddChild( child, index);
        
        // unlock state and end update
        model.unlock( this); 
        model.unlock( oldParent); 
        model.unlock( child);
        if ( model != null) model.endUpdate();
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
    if ( children == null) children = new ArrayList<IModelObject>( 1);
    if ( index >= 0) children.add( index, child); else children.add( child);
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#removeChild(int)
   */
  public IModelObject removeChild( int index)
  {
    IChangeSet transaction = getModel().isLocked( this);
    if ( transaction != null)
    {
      // bail if no children
      if ( children == null) return null;
      
      // create change record
      IModelObject child = getChild( index);
      transaction.removeChild( this, child, index);
      return child;
    }
    else
    {
      writeChildrenAccess();
      
      // bail if no children
      if ( children == null) return null;
      
      IModelObject child = children.get( index);
      if ( child != null)
      {
        IModel model = getModel();
        if ( model != null)
        {
          Update update = model.startUpdate();
          update.removeChild( this, child, index);
        }
        removeChildImpl( index);
        child.internal_setParent( null);
        
        // lock state and notify
        model.lock( this);
        model.lock( child);
        notifyRemoveChild( child, index);
        
        // unlock state and end update
        model.unlock( this);
        model.unlock( child);
        if ( model != null) model.endUpdate();
      }
      return child;
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#removeChild(dunnagan.bob.xmodel.IModelObject)
   */
  public void removeChild( IModelObject child)
  {
    IChangeSet transaction = getModel().isLocked( this);
    if ( transaction != null)
    {
      transaction.removeChild( this, child);
    }
    else
    {
      writeChildrenAccess();
      
      // bail if no children
      if ( children == null) return;
      
      int index = children.indexOf( child);
      if ( index >= 0)
      {
        IModel model = getModel();
        if ( model != null)
        {
          Update update = model.startUpdate();
          update.removeChild( this, child, index);
        }
        removeChildImpl( index);
        child.internal_setParent( null);

        // lock state and notify
        model.lock( this);
        model.lock( child);
        notifyRemoveChild( child, index);
        
        // unlock state and end update
        model.unlock( this);
        model.unlock( child);
        if ( model != null) model.endUpdate();
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
    if ( children == null) return null;
    IModelObject child = children.remove( index);
    if ( children.size() == 0) children = null;
    return child;
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#removeChildren()
   */
  public void removeChildren()
  {
    while( children != null && children.size() > 0)
      removeChild( children.size()-1);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#removeChildren(java.lang.String)
   */
  public void removeChildren( String type)
  {
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
   * @see dunnagan.bob.xmodel.IModelObject#removeFromParent()
   */
  public void removeFromParent()
  {
    IModelObject parent = getParent();
    if ( parent != null) parent.removeChild( this);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getChild(int)
   */
  public IModelObject getChild( int index)
  {
    readChildrenAccess();
    
    // bail if no children
    if ( children == null) return null;
    
    // bail if index too large
    if ( index >= children.size()) return null;
    
    // get child
    return children.get( index);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getFirstChild(java.lang.String)
   */
  public IModelObject getFirstChild( String type)
  {
    readChildrenAccess();
    
    if ( children != null)
    {
      for( IModelObject child: children)
        if ( child.isType( type))
          return child;
    }
    return null;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getChild(java.lang.String, java.lang.String)
   */
  public IModelObject getChild( String type, String name)
  {
    readChildrenAccess();
    
    if ( children != null)
    {
      for( IModelObject child: children)
        if ( child.isType( type) && child.getID().equals( name))
          return child;
    }
    return null;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getCreateChild(java.lang.String)
   */
  public IModelObject getCreateChild( String type)
  {
    IModelObject child = getFirstChild( type);
    if ( child == null)
    {
      child = new ModelObject( type);
      addChild( child);
    }
    return child;
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getCreateChild(java.lang.String, java.lang.String)
   */
  public IModelObject getCreateChild( String type, String name)
  {
    IModelObject child = getChild( type, name);
    if ( child == null) 
    {
      child = new ModelObject( type);
      child.setID( name);
      addChild( child);
    }
    return child;
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getChildren(java.lang.String, java.lang.String)
   */
  public List<IModelObject> getChildren( String type, String name)
  {
    readChildrenAccess();
    
    List<IModelObject> result = new ArrayList<IModelObject>( 1);
    if ( children != null)
    {
      for( IModelObject child: children)
        if ( child.isType( type) && child.getID().equals( name))
          result.add( child);
    }
    return result;
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getChildren()
   */
  public List<IModelObject> getChildren()
  {
    readChildrenAccess();
    List<IModelObject> result = children;
    if ( children == null) result = Collections.emptyList();
    return result;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getChildren(java.lang.String)
   */
  public List<IModelObject> getChildren( String type)
  {
    readChildrenAccess();
    
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
   * @see dunnagan.bob.xmodel.IModelObject#getTypesOfChildren()
   */
  public Set<String> getTypesOfChildren()
  {
    readChildrenAccess();
    
    HashSet<String> set = new HashSet<String>();
    if ( children != null)
    {
      for( IModelObject child: children)
        set.add( child.getType());
    }
    return set;   
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getNumberOfChildren()
   */
  public int getNumberOfChildren()
  {
    readChildrenAccess();
    return (children == null)? 0: children.size();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getNumberOfChildren(java.lang.String)
   */
  public int getNumberOfChildren( String type)
  {
    readChildrenAccess();
    return getChildren( type).size();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#internal_notifyParent(dunnagan.bob.xmodel.IModelObject, dunnagan.bob.xmodel.IModelObject)
   */
  public void internal_notifyParent( IModelObject newParent, IModelObject oldParent)
  {
    notifyParent( newParent, oldParent);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#internal_notifyAdd(dunnagan.bob.xmodel.IModelObject, int)
   */
  public void internal_notifyAddChild( IModelObject child, int index)
  {
    notifyAddChild( child, index);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#internal_notifyRemove(dunnagan.bob.xmodel.IModelObject, int)
   */
  public void internal_notifyRemoveChild( IModelObject child, int index)
  {
    notifyRemoveChild( child, index);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#internal_addChild(dunnagan.bob.xmodel.IModelObject, int)
   */
  public void internal_addChild( IModelObject child, int index)
  {
    addChildImpl( child, index);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#internal_removeChild(int)
   */
  public IModelObject internal_removeChild( int index)
  {
    return removeChildImpl( index);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#internal_setParent(dunnagan.bob.xmodel.IModelObject)
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
   * @see dunnagan.bob.xmodel.IModelObject#getParent()
   */
  public IModelObject getParent()
  {
    return parent;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getAncestor(java.lang.String)
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
   * @see dunnagan.bob.xmodel.IModelObject#getRoot()
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
   * @see dunnagan.bob.xmodel.IModelObject#identityPath()
   */
  public IPath identityPath()
  {
    return ModelAlgorithms.createIdentityPath( this);
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#typePath()
   */
  public IPath typePath()
  {
    return ModelAlgorithms.createTypePath( this);
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#peerPath(dunnagan.bob.xmodel.IModelObject)
   */
  public IPath peerPath( IModelObject peer)
  {
    return ModelAlgorithms.createRelativePath( this, peer);
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#addListener(dunnagan.bob.xmodel.IModelListener)
   */
  public void addModelListener( IModelListener listener)
  {
    if ( listeners == null) listeners = new ModelListenerList();
    listeners.addListener( listener);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#removeListener(dunnagan.bob.xmodel.IModelListener)
   */
  public void removeModelListener( IModelListener listener)
  {
    if ( listeners == null) return;
    listeners.removeListener( listener);
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getModelListeners()
   */
  public ModelListenerList getModelListeners()
  {
    return listeners;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getPathListeners()
   */
  public PathListenerList getPathListeners()
  {
    if ( pathListeners == null) pathListeners = new PathListenerList();
    return pathListeners;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#addAncestorListener(dunnagan.bob.xmodel.IAncestorListener)
   */
  public void addAncestorListener( IAncestorListener listener)
  {
    ClimbingListener climber = new ClimbingListener( listener);
    climber.addListenerToTree( this);
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#removeAncestorListener(dunnagan.bob.xmodel.IAncestorListener)
   */
  public void removeAncestorListener( IAncestorListener listener)
  {
    // this works because ClimbingListener implements the equals method
    ClimbingListener climber = new ClimbingListener( listener);
    climber.removeListenerFromTree( this);
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#cloneObject()
   */
  public IModelObject cloneObject()
  {
    IModelObject clone = new ModelObject( getType());
    ModelAlgorithms.copyAttributes( this, clone);
    return clone;
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#cloneTree()
   */
  public IModelObject cloneTree()
  {
    return ModelAlgorithms.cloneTree( this);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getReferent()
   */
  public IModelObject getReferent()
  {
    return this;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#revertUpdate(dunnagan.bob.xmodel.memento.IMemento)
   */
  public void revertUpdate( IMemento iMemento)
  {
    if ( iMemento instanceof SetAttributeMemento)
    {
      SetAttributeMemento memento = (SetAttributeMemento)iMemento;
      if ( memento.oldValue == null)
        attributes.remove( memento.attrName);
      else
        attributes.put( memento.attrName, memento.oldValue);
    }
    else if ( iMemento instanceof SetParentMemento)
    {
      SetParentMemento memento = (SetParentMemento)iMemento;
      parent = memento.oldParent;
    }
    else if ( iMemento instanceof AddChildMemento)
    {
      AddChildMemento memento = (AddChildMemento)iMemento;
      children.remove( memento.index);
    }
    else if ( iMemento instanceof RemoveChildMemento)
    {
      RemoveChildMemento memento = (RemoveChildMemento)iMemento;
      if ( children == null) children = new ArrayList<IModelObject>( 1);
      children.add( memento.index, memento.child);
    }
    else
    {
      RemoveAttributeMemento memento = (RemoveAttributeMemento)iMemento;
      attributes.put( memento.attrName, memento.oldValue);
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#restoreUpdate(dunnagan.bob.xmodel.memento.IMemento)
   */
  public void restoreUpdate( IMemento iMemento)
  {
    if ( iMemento instanceof SetAttributeMemento)
    {
      SetAttributeMemento memento = (SetAttributeMemento)iMemento;
      attributes.put( memento.attrName, memento.newValue);
    }
    else if ( iMemento instanceof SetParentMemento)
    {
      SetParentMemento memento = (SetParentMemento)iMemento;
      parent = memento.newParent;
    }
    else if ( iMemento instanceof AddChildMemento)
    {
      AddChildMemento memento = (AddChildMemento)iMemento;
      children.add( memento.index, memento.child);
    }
    else if ( iMemento instanceof RemoveChildMemento)
    {
      RemoveChildMemento memento = (RemoveChildMemento)iMemento;
      children.remove( memento.index);
    }
    else
    {
      RemoveAttributeMemento memento = (RemoveAttributeMemento)iMemento;
      attributes.remove( memento.attrName);
    }
  }

  /**
   * Convenience method for notifying subclasses when this object has been read accessed. This
   * method is not called when the <code>getType</code> or <code>isType</code> methods are
   * called since the type of the object never changes. The attrName will be null if all of
   * the attributes are being accessed.
   * @param attrName The name of the attribute being read or null.
   */
  protected void readAttributeAccess( String attrName)
  {
  }
  
  /**
   * Convenience method for notifying subclasses when the list of children of this object is
   * accessed for reading. This method is called first so that the children can be populated
   * if they have not been already.
   */
  protected void readChildrenAccess()
  {
  }
  
  /**
   * Convenience method for notifying subclasses when this object has been write accessed. This
   * method is not called when the parent is changed.
   * @param attrName The name of the attribute.
   */
  protected void writeAttributeAccess( String attrName)
  {
  }
  
  /**
   * Convenience method for notifying subclasses when the list of children of this object is
   * accessed for writing. This method is called first so that the children can be populated
   * if they have not been already.
   */
  protected void writeChildrenAccess()
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

    // attributes
    if ( attributes != null)
    {
      for( Map.Entry<String, Object> entry: attributes.entrySet())
      {
        builder.append( ' ');
        builder.append( entry.getKey());
        builder.append( "='");
        builder.append( entry.getValue());
        builder.append( '\'');
      }
    }
    
    // children
    if ( children != null && children.size() > 0)
      builder.append( ">...</>");
    else
      builder.append( "/>");

    return builder.toString();
  }
  
  /**
   * Returns the xml representation of the subtree.
   * @return Returns the xml representation of the subtree.
   */
  public String toXml()
  {
    IModel model = getModel();
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

  private IModel model;
  private String type;
  private IModelObject parent;
  private List<IModelObject> children;
  private Map<String, Object> attributes;
  private ModelListenerList listeners;
  private PathListenerList pathListeners;
}
