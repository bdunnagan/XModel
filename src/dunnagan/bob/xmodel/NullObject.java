/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import dunnagan.bob.xmodel.memento.IMemento;

public class NullObject implements IModelObject
{
  public NullObject()
  {
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#isDirty()
   */
  public boolean isDirty()
  {
    return false;
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
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#addChild(dunnagan.bob.xmodel.IModelObject, int)
   */
  public void addChild( IModelObject object, int index)
  {
    throw new UnsupportedOperationException();
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
   * @see dunnagan.bob.xmodel.IModelObject#cloneObject()
   */
  public IModelObject cloneObject()
  {
    return new NullObject();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#cloneTree()
   */
  public IModelObject cloneTree()
  {
    return cloneObject();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#createObject(java.lang.String)
   */
  public IModelObject createObject( String type)
  {
    throw new UnsupportedOperationException();
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
    return null;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getAttributeNames()
   */
  public Collection<String> getAttributeNames()
  {
    return Collections.emptySet();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getAttributeNode(java.lang.String)
   */
  public IModelObject getAttributeNode( String attrName)
  {
    return null;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getChild(int)
   */
  public IModelObject getChild( int index)
  {
    return null;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getChild(java.lang.String, java.lang.String)
   */
  public IModelObject getChild( String type, String name)
  {
    return null;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getChildValue(java.lang.String)
   */
  public Object getChildValue( String type)
  {
    return "";
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getChildren(java.lang.String, java.lang.String)
   */
  public List<IModelObject> getChildren( String type, String name)
  {
    return Collections.emptyList();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getChildren()
   */
  public List<IModelObject> getChildren()
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
   * @see dunnagan.bob.xmodel.IModelObject#getCreateChild(java.lang.String)
   */
  public IModelObject getCreateChild( String type)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getCreateChild(java.lang.String, java.lang.String)
   */
  public IModelObject getCreateChild( String type, String name)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getFirstChild(java.lang.String)
   */
  public IModelObject getFirstChild( String type)
  {
    return null;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getID()
   */
  public String getID()
  {
    return "";
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getModel()
   */
  public IModel getModel()
  {
    return ModelRegistry.getInstance().getModel();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getModelListeners()
   */
  public ModelListenerList getModelListeners()
  {
    return new ModelListenerList();
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
   * @see dunnagan.bob.xmodel.IModelObject#getPathListeners()
   */
  public PathListenerList getPathListeners()
  {
    return new PathListenerList();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getReferent()
   */
  public IModelObject getReferent()
  {
    return this;
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
    return "";
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getTypesOfChildren()
   */
  public Set<String> getTypesOfChildren()
  {
    return Collections.emptySet();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#getValue()
   */
  public Object getValue()
  {
    return null;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#identityPath()
   */
  public IPath identityPath()
  {
    return ModelAlgorithms.createIdentityPath( this);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#internal_setParent(dunnagan.bob.xmodel.IModelObject)
   */
  public IModelObject internal_setParent( IModelObject parent)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#internal_addChild(dunnagan.bob.xmodel.IModelObject, int)
   */
  public void internal_addChild( IModelObject child, int index)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#internal_notifyAddChild(dunnagan.bob.xmodel.IModelObject, int)
   */
  public void internal_notifyAddChild( IModelObject child, int index)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#internal_notifyParent(dunnagan.bob.xmodel.IModelObject, dunnagan.bob.xmodel.IModelObject)
   */
  public void internal_notifyParent( IModelObject newParent, IModelObject oldParent)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#internal_notifyRemoveChild(dunnagan.bob.xmodel.IModelObject, int)
   */
  public void internal_notifyRemoveChild( IModelObject child, int index)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#internal_removeChild(int)
   */
  public IModelObject internal_removeChild( int index)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#isType(java.lang.String)
   */
  public boolean isType( String type)
  {
    return type == null || type.length() == 0;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#peerPath(dunnagan.bob.xmodel.IModelObject)
   */
  public IPath peerPath( IModelObject peer)
  {
    return ModelAlgorithms.createRelativePath( this, peer);
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
   * @see dunnagan.bob.xmodel.IModelObject#restoreUpdate(dunnagan.bob.xmodel.memento.IMemento)
   */
  public void restoreUpdate( IMemento memento)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#revertUpdate(dunnagan.bob.xmodel.memento.IMemento)
   */
  public void revertUpdate( IMemento memento)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#setAttribute(java.lang.String)
   */
  public Object setAttribute( String attrName)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#setAttribute(java.lang.String, java.lang.Object)
   */
  public Object setAttribute( String attrName, Object attrValue)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#setID(java.lang.String)
   */
  public void setID( String id)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#setValue(java.lang.Object)
   */
  public Object setValue( Object value)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObject#typePath()
   */
  public IPath typePath()
  {
    return ModelAlgorithms.createTypePath( this);
  }
}
