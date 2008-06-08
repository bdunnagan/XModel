/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xpath;

import java.util.*;

import dunnagan.bob.xmodel.*;
import dunnagan.bob.xmodel.memento.IMemento;

/**
 * An implementation of IModelObject which serves as a light-weight container for
 * attributes of other IModelObjects.  This container is used during the evaluation
 * of X-Path expressions to hold the old value of an attribute after it has been changed.
 */
public class AttributeHistoryNode implements IModelObject
{
  /**
   * Create an Attribute to hold the given attribute information. 
   * @param attrName The name of the attribute.
   * @parma attrValue The old value of the attribute.
   */
  public AttributeHistoryNode( String attrName, Object attrValue)
  {
    this.attrName = attrName;
    this.attrValue = attrValue;
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getModel()
   */
  public IModel getModel()
  {
    return null;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getID()
   */
  public String getID()
  {
    throw new UnsupportedOperationException();
    //return attrName;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#setID(java.lang.String)
   */
  public void setID( String id)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#addAncestorListener(dunnagan.bob.xmodel.IAncestorListener)
   */
  public void addAncestorListener( IAncestorListener listener)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#addChild(dunnagan.bob.xmodel.IModelObject)
   */
  public void addChild( IModelObject object)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#addChild(dunnagan.bob.xmodel.IModelObject, int)
   */
  public void addChild( IModelObject object, int index)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getChild(int)
   */
  public IModelObject getChild( int index)
  {
    return null;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#removeChild(int)
   */
  public IModelObject removeChild( int index)
  {
    return null;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#addModelListener(dunnagan.bob.xmodel.IModelListener)
   */
  public void addModelListener( IModelListener listener)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#addModelListener(int, dunnagan.bob.xmodel.IModelListener)
   */
  public void addModelListener( int priority, IModelListener listener)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#addPathListener(dunnagan.bob.xmodel.IPath, dunnagan.bob.xmodel.IPathListener)
   */
  public void addPathListener( IPath path, IPathListener listener)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#cloneObject()
   */
  public IModelObject cloneObject()
  {
    return new AttributeHistoryNode( attrName, attrValue);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#cloneTree()
   */
  public IModelObject cloneTree()
  {
    return cloneObject();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getReferent()
   */
  public IModelObject getReferent()
  {
    return this;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getAncestor(java.lang.String)
   */
  public IModelObject getAncestor( String type)
  {
    return null;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getAttribute(java.lang.String)
   */
  public Object getAttribute( String attrName)
  {
    // return attribute if queried by attrName or node 'value'
    if ( attrName.length() == 0 || attrName.equals( this.attrName)) return attrValue;
    return null;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getAttributeNode(java.lang.String)
   */
  public IModelObject getAttributeNode( String attrName)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getAttributeNames()
   */
  public Collection<String> getAttributeNames()
  {
    List<String> names = new ArrayList<String>( 1);
    names.add( attrName);
    return names;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getChild(java.lang.String, java.lang.String)
   */
  public IModelObject getChild( String type, String name)
  {
    return null;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getChildren()
   */
  public List<IModelObject> getChildren()
  {
    return Collections.emptyList();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getChildren(java.lang.String, java.lang.String)
   */
  public List<IModelObject> getChildren( String type, String name)
  {
    return Collections.emptyList();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getChildren(java.lang.String)
   */
  public List<IModelObject> getChildren( String type)
  {
    return Collections.emptyList();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getValue()
   */
  public Object getValue()
  {
    return attrValue;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getValueNode()
   */
  public IModelObject getValueNode()
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getChildValue(java.lang.String)
   */
  public Object getChildValue( String type)
  {
    return null;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getCreateChild(java.lang.String, java.lang.String)
   */
  public IModelObject getCreateChild( String type, String name)
  {
    return null;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getCreateChild(java.lang.String)
   */
  public IModelObject getCreateChild( String type)
  {
    return null;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getFirstChild(java.lang.String)
   */
  public IModelObject getFirstChild( String type)
  {
    return null;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getModelListeners()
   */
  public ModelListenerList getModelListeners()
  {
    return null;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getPathListeners()
   */
  public PathListenerList getPathListeners()
  {
    return null;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getName()
   */
  public String getName()
  {
    throw new UnsupportedOperationException();
    //return attrName;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getNumberOfChildren()
   */
  public int getNumberOfChildren()
  {
    return 0;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getNumberOfChildren(java.lang.String)
   */
  public int getNumberOfChildren( String type)
  {
    return 0;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getParent()
   */
  public IModelObject getParent()
  {
    return null;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getRoot()
   */
  public IModelObject getRoot()
  {
    return null;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getType()
   */
  public String getType()
  {
    return attrName;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getTypesOfChildren()
   */
  public Set<String> getTypesOfChildren()
  {
    return Collections.emptySet();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#identityPath()
   */
  public IPath identityPath()
  {
    return null;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#internal_setParent(dunnagan.bob.xmodel.IModelObject)
   */
  public IModelObject internal_setParent( IModelObject parent)
  {
    return null;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#internal_notifyParent(dunnagan.bob.xmodel.IModelObject, dunnagan.bob.xmodel.IModelObject)
   */
  public void internal_notifyParent( IModelObject newParent, IModelObject oldParent)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#internal_notifyAddChild(dunnagan.bob.xmodel.IModelObject, int)
   */
  public void internal_notifyAddChild( IModelObject child, int index)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#internal_notifyRemoveChild(dunnagan.bob.xmodel.IModelObject, int)
   */
  public void internal_notifyRemoveChild( IModelObject child, int index)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#internal_addChild(dunnagan.bob.xmodel.IModelObject, int)
   */
  public void internal_addChild( IModelObject child, int index)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#internal_removeChild(int)
   */
  public IModelObject internal_removeChild( int index)
  {
    return null;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#isDirty()
   */
  public boolean isDirty()
  {
    return false;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#isType(java.lang.String)
   */
  public boolean isType( String type)
  {
    return type.equals( attrName);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#peerPath(dunnagan.bob.xmodel.IModelObject)
   */
  public IPath peerPath( IModelObject peer)
  {
    return null;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#removeAncestorListener(dunnagan.bob.xmodel.IAncestorListener)
   */
  public void removeAncestorListener( IAncestorListener listener)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#removeAttribute(java.lang.String)
   */
  public Object removeAttribute( String attrName)
  {
    if ( attrName.equals( this.attrName)) return attrValue;
    return null;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#removeChild(dunnagan.bob.xmodel.IModelObject)
   */
  public void removeChild( IModelObject object)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#removeChildren()
   */
  public void removeChildren()
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#removeChildren(java.lang.String)
   */
  public void removeChildren( String type)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#removeFromParent()
   */
  public void removeFromParent()
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#removeModelListener(dunnagan.bob.xmodel.IModelListener)
   */
  public void removeModelListener( IModelListener listener)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#removePathListener(dunnagan.bob.xmodel.IPath, dunnagan.bob.xmodel.IPathListener)
   */
  public void removePathListener( IPath path, IPathListener listener)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#setAttribute(java.lang.String, java.lang.Object)
   */
  public Object setAttribute( String attrName, Object attrValue)
  {
    if ( attrName.equals( this.attrName)) return this.attrValue;
    return null;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#setAttribute(java.lang.String)
   */
  public Object setAttribute( String attrName)
  {
    if ( attrName.equals( this.attrName)) return this.attrValue;
    return null;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#setName(java.lang.String)
   */
  public void setName( String name)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#setValue(java.lang.Object)
   */
  public Object setValue( Object value)
  {
    return attrValue;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#typePath()
   */
  public IPath typePath()
  {
    return null;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#revertUpdate(dunnagan.bob.xmodel.memento.IMemento)
   */
  public void revertUpdate( IMemento memento)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#restoreUpdate(dunnagan.bob.xmodel.memento.IMemento)
   */
  public void restoreUpdate( IMemento memento)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals( Object object)
  {
    if ( object instanceof IModelObject)
    {
      IModelObject node = (IModelObject)object;
      return node.isType( attrName);
    }
    return super.equals( object);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  public int hashCode()
  {
    return attrName.hashCode();
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return (attrValue == null)? "": attrValue.toString();
  }

  String attrName;
  Object attrValue;
}
