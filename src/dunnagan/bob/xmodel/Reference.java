/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import dunnagan.bob.xmodel.memento.IMemento;
import dunnagan.bob.xmodel.memento.SetParentMemento;

/**
 * An implementation of IModelObject which functions as a reference to another IModelObject.
 */
public class Reference implements IModelObject
{
  /**
   * Create a HardReference with the specified referent.
   * @param referent The referent to which the reference points.
   */
  public Reference( IModelObject referent)
  {
    this.type = referent.getType();
    this.referent = referent;
  }
  
  /**
   * Create a HardReference with the specified type and referent.
   * @param type The type associated with the reference instance.
   * @param referent The referent to which the reference points.
   */
  public Reference( String type, IModelObject referent)
  {
    this.type = type;
    this.referent = referent;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getModel()
   */
  public IModel getModel()
  {
    return referent.getModel();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#setName(java.lang.String)
   */
  public void setName( String name)
  {
    referent.setID( name);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getName()
   */
  public String getName()
  {
    return referent.getID();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getID()
   */
  public String getID()
  {
    return referent.getID();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#setID(java.lang.String)
   */
  public void setID( String id)
  {
    referent.setID( id);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getType()
   */
  public String getType()
  {
    return referent.getType();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#isType(java.lang.String)
   */
  public boolean isType( String type)
  {
    return referent.isType( type);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#isDirty()
   */
  public boolean isDirty()
  {
    return referent.isDirty();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#setAttribute(java.lang.String)
   */
  public Object setAttribute( String attrName)
  {
    return referent.setAttribute( attrName);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#setAttribute(java.lang.String, java.lang.Object)
   */
  public Object setAttribute( String attrName, Object attrValue)
  {
    return referent.setAttribute( attrName, attrValue);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getAttribute(java.lang.String)
   */
  public Object getAttribute( String attrName)
  {
    return referent.getAttribute( attrName);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getAttributeNode(java.lang.String)
   */
  public IModelObject getAttributeNode( String attrName)
  {
    return referent.getAttributeNode( attrName);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getAttributeNames()
   */
  public Collection<String> getAttributeNames()
  {
    return referent.getAttributeNames();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#removeAttribute(java.lang.String)
   */
  public Object removeAttribute( String attrName)
  {
    return referent.removeAttribute( attrName);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#setValue(java.lang.Object)
   */
  public Object setValue( Object value)
  {
    return referent.setValue( value);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getValue()
   */
  public Object getValue()
  {
    return referent.getValue();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getChildValue(java.lang.String)
   */
  public Object getChildValue( String type)
  {
    return referent.getChildValue( type);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#addChild(dunnagan.bob.xmodel.IModelObject)
   */
  public void addChild( IModelObject object)
  {
    referent.addChild( object);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#addChild(dunnagan.bob.xmodel.IModelObject, int)
   */
  public void addChild( IModelObject object, int index)
  {
    referent.addChild( object, index);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#removeChild(int)
   */
  public IModelObject removeChild( int index)
  {
    return referent.removeChild( index);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#removeChild(dunnagan.bob.xmodel.IModelObject)
   */
  public void removeChild( IModelObject object)
  {
    referent.removeChild( object);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#removeChildren()
   */
  public void removeChildren()
  {
    referent.removeChildren();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#removeChildren(java.lang.String)
   */
  public void removeChildren( String type)
  {
    referent.removeChildren( type);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#removeFromParent()
   */
  public void removeFromParent()
  {
    if ( parent != null) parent.removeChild( this);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getChild(int)
   */
  public IModelObject getChild( int index)
  {
    return referent.getChild( index);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getFirstChild(java.lang.String)
   */
  public IModelObject getFirstChild( String type)
  {
    return referent.getFirstChild( type);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getChild(java.lang.String, java.lang.String)
   */
  public IModelObject getChild( String type, String name)
  {
    return referent.getChild( type, name);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getCreateChild(java.lang.String)
   */
  public IModelObject getCreateChild( String type)
  {
    return referent.getCreateChild( type);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getCreateChild(java.lang.String, java.lang.String)
   */
  public IModelObject getCreateChild( String type, String name)
  {
    return referent.getCreateChild( type, name);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getChildren(java.lang.String, java.lang.String)
   */
  public List<IModelObject> getChildren( String type, String name)
  {
    return referent.getChildren( type, name);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getChildren()
   */
  public List<IModelObject> getChildren()
  {
    return referent.getChildren();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getChildren(java.lang.String)
   */
  public List<IModelObject> getChildren( String type)
  {
    return referent.getChildren( type);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getTypesOfChildren()
   */
  public Set<String> getTypesOfChildren()
  {
    return referent.getTypesOfChildren();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getNumberOfChildren()
   */
  public int getNumberOfChildren()
  {
    return referent.getNumberOfChildren();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getNumberOfChildren(java.lang.String)
   */
  public int getNumberOfChildren( String type)
  {
    return referent.getNumberOfChildren( type);
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

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#internal_notifyParent(dunnagan.bob.xmodel.IModelObject, dunnagan.bob.xmodel.IModelObject)
   */
  public void internal_notifyParent( IModelObject newParent, IModelObject oldParent)
  {
    // FIXME: should Reference keep listeners so that it can perform notification of parent?
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#internal_notifyAddChild(dunnagan.bob.xmodel.IModelObject, int)
   */
  public void internal_notifyAddChild( IModelObject child, int index)
  {
    referent.internal_notifyAddChild( child, index);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#internal_notifyRemoveChild(dunnagan.bob.xmodel.IModelObject, int)
   */
  public void internal_notifyRemoveChild( IModelObject child, int index)
  {
    referent.internal_notifyRemoveChild( child, index);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#internal_addChild(dunnagan.bob.xmodel.IModelObject, int)
   */
  public void internal_addChild( IModelObject child, int index)
  {
    referent.internal_addChild( child, index);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#internal_removeChild(int)
   */
  public IModelObject internal_removeChild( int index)
  {
    return referent.internal_removeChild( index);
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
    if ( parent.isType( type)) return parent;
    return parent.getAncestor( type);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getRoot()
   */
  public IModelObject getRoot()
  {
    if ( parent == null) return this;
    return parent.getRoot();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#identityPath()
   */
  public IPath identityPath()
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#typePath()
   */
  public IPath typePath()
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#peerPath(dunnagan.bob.xmodel.IModelObject)
   */
  public IPath peerPath( IModelObject peer)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#addModelListener(dunnagan.bob.xmodel.IModelListener)
   */
  public void addModelListener( IModelListener listener)
  {
    referent.addModelListener( listener);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#removeModelListener(dunnagan.bob.xmodel.IModelListener)
   */
  public void removeModelListener( IModelListener listener)
  {
    referent.removeModelListener( listener);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getModelListeners()
   */
  public ModelListenerList getModelListeners()
  {
    return referent.getModelListeners();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getPathListeners()
   */
  public PathListenerList getPathListeners()
  {
    return referent.getPathListeners();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#addAncestorListener(dunnagan.bob.xmodel.IAncestorListener)
   */
  public void addAncestorListener( IAncestorListener listener)
  {
    referent.addAncestorListener( listener);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#removeAncestorListener(dunnagan.bob.xmodel.IAncestorListener)
   */
  public void removeAncestorListener( IAncestorListener listener)
  {
    referent.removeAncestorListener( listener);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#cloneObject()
   */
  public IModelObject cloneObject()
  {
    return referent.cloneObject();
  }

  /* (non-Javadoc)
   */
  public IModelObject cloneTree()
  {
    return referent.cloneTree();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getReferent()
   */
  public IModelObject getReferent()
  {
    return referent;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#revertUpdate(dunnagan.bob.xmodel.memento.IMemento)
   */
  public void revertUpdate( IMemento iMemento)
  {
    if ( iMemento instanceof SetParentMemento)
    {
      SetParentMemento memento = (SetParentMemento)iMemento;
      parent = memento.oldParent;
    }
    else
    {
      referent.revertUpdate( iMemento);
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#restoreUpdate(dunnagan.bob.xmodel.memento.IMemento)
   */
  public void restoreUpdate( IMemento iMemento)
  {
    if ( iMemento instanceof SetParentMemento)
    {
      SetParentMemento memento = (SetParentMemento)iMemento;
      parent = memento.newParent;
    }
    else
    {
      referent.restoreUpdate( iMemento);
    }
  }

  /**
   * Traverse all references until the ultimate referent is found.
   * @param object The starting point reference.
   * @return Returns the ultimate referent.
   */
  public static IModelObject getReferent( IModelObject object)
  {
    IModelObject referent = object.getReferent();
    while( referent != object) 
    {
      object = referent;
      referent = object.getReferent();
    }
    return referent;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals( Object object)
  {
    try
    {
      if ( referent == null) return super.equals( object);
      return getReferent( referent) == getReferent( (IModelObject)object);
    }
    catch( ClassCastException e)
    {
      return false;
    }
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  public int hashCode()
  {
    if ( referent == null) return super.hashCode();
    return getReferent( referent).hashCode();
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return "&"+referent;
  }
  
  private String type;
  private IModelObject parent;
  private IModelObject referent;
}
