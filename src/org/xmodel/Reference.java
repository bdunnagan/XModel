/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * Reference.java
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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.xmodel.external.CachingException;
import org.xmodel.external.ICachingPolicy;
import org.xmodel.external.ITransaction;
import org.xmodel.memento.IMemento;
import org.xmodel.memento.SetParentMemento;


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
    this.referent = referent;
  }
  
  /**
   * Create a HardReference with the specified type and referent.
   * @param type The type associated with the reference instance.
   * @param referent The referent to which the reference points.
   */
  public Reference( String type, IModelObject referent)
  {
    this.referent = referent;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getID()
   */
  public String getID()
  {
    return referent.getID();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#setID(java.lang.String)
   */
  public void setID( String id)
  {
    referent.setID( id);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getType()
   */
  public String getType()
  {
    return referent.getType();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#isType(java.lang.String)
   */
  public boolean isType( String type)
  {
    return referent.isType( type);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#isDirty()
   */
  public boolean isDirty()
  {
    return referent.isDirty();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#setAttribute(java.lang.String)
   */
  public Object setAttribute( String attrName)
  {
    return referent.setAttribute( attrName);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#setAttribute(java.lang.String, java.lang.Object)
   */
  public Object setAttribute( String attrName, Object attrValue)
  {
    return referent.setAttribute( attrName, attrValue);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getAttribute(java.lang.String)
   */
  public Object getAttribute( String attrName)
  {
    return referent.getAttribute( attrName);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getAttributeNode(java.lang.String)
   */
  public IModelObject getAttributeNode( String attrName)
  {
    return referent.getAttributeNode( attrName);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getAttributeNames()
   */
  public Collection<String> getAttributeNames()
  {
    return referent.getAttributeNames();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#removeAttribute(java.lang.String)
   */
  public Object removeAttribute( String attrName)
  {
    return referent.removeAttribute( attrName);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#setValue(java.lang.Object)
   */
  public Object setValue( Object value)
  {
    return referent.setValue( value);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getValue()
   */
  public Object getValue()
  {
    return referent.getValue();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getChildValue(java.lang.String)
   */
  public Object getChildValue( String type)
  {
    return referent.getChildValue( type);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#addChild(org.xmodel.IModelObject)
   */
  public void addChild( IModelObject object)
  {
    referent.addChild( object);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#addChild(org.xmodel.IModelObject, int)
   */
  public void addChild( IModelObject object, int index)
  {
    referent.addChild( object, index);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#removeChild(int)
   */
  public IModelObject removeChild( int index)
  {
    return referent.removeChild( index);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#removeChild(org.xmodel.IModelObject)
   */
  public void removeChild( IModelObject object)
  {
    referent.removeChild( object);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#removeChildren()
   */
  public void removeChildren()
  {
    referent.removeChildren();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#removeChildren(java.lang.String)
   */
  public void removeChildren( String type)
  {
    referent.removeChildren( type);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#removeFromParent()
   */
  public void removeFromParent()
  {
    if ( parent != null) parent.removeChild( this);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getChild(int)
   */
  public IModelObject getChild( int index)
  {
    return referent.getChild( index);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getFirstChild(java.lang.String)
   */
  public IModelObject getFirstChild( String type)
  {
    return referent.getFirstChild( type);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getChild(java.lang.String, java.lang.String)
   */
  public IModelObject getChild( String type, String name)
  {
    return referent.getChild( type, name);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getCreateChild(java.lang.String)
   */
  public IModelObject getCreateChild( String type)
  {
    return referent.getCreateChild( type);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getCreateChild(java.lang.String, java.lang.String)
   */
  public IModelObject getCreateChild( String type, String name)
  {
    return referent.getCreateChild( type, name);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getChildren(java.lang.String, java.lang.String)
   */
  public List<IModelObject> getChildren( String type, String name)
  {
    return referent.getChildren( type, name);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getChildren()
   */
  public List<IModelObject> getChildren()
  {
    return referent.getChildren();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getChildren(java.lang.String)
   */
  public List<IModelObject> getChildren( String type)
  {
    return referent.getChildren( type);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getTypesOfChildren()
   */
  public Set<String> getTypesOfChildren()
  {
    return referent.getTypesOfChildren();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getNumberOfChildren()
   */
  public int getNumberOfChildren()
  {
    return referent.getNumberOfChildren();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getNumberOfChildren(java.lang.String)
   */
  public int getNumberOfChildren( String type)
  {
    return referent.getNumberOfChildren( type);
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

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#internal_notifyParent(org.xmodel.IModelObject, org.xmodel.IModelObject)
   */
  public void internal_notifyParent( IModelObject newParent, IModelObject oldParent)
  {
    // FIXME: should Reference keep listeners so that it can perform notification of parent?
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#internal_notifyAddChild(org.xmodel.IModelObject, int)
   */
  public void internal_notifyAddChild( IModelObject child, int index)
  {
    referent.internal_notifyAddChild( child, index);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#internal_notifyRemoveChild(org.xmodel.IModelObject, int)
   */
  public void internal_notifyRemoveChild( IModelObject child, int index)
  {
    referent.internal_notifyRemoveChild( child, index);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#internal_addChild(org.xmodel.IModelObject, int)
   */
  public void internal_addChild( IModelObject child, int index)
  {
    referent.internal_addChild( child, index);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#internal_removeChild(int)
   */
  public IModelObject internal_removeChild( int index)
  {
    return referent.internal_removeChild( index);
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
    if ( parent.isType( type)) return parent;
    return parent.getAncestor( type);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getRoot()
   */
  public IModelObject getRoot()
  {
    if ( parent == null) return this;
    return parent.getRoot();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#addModelListener(org.xmodel.IModelListener)
   */
  public void addModelListener( IModelListener listener)
  {
    referent.addModelListener( listener);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#removeModelListener(org.xmodel.IModelListener)
   */
  public void removeModelListener( IModelListener listener)
  {
    referent.removeModelListener( listener);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getModelListeners()
   */
  public ModelListenerList getModelListeners()
  {
    return referent.getModelListeners();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getPathListeners()
   */
  public PathListenerList getPathListeners()
  {
    return referent.getPathListeners();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#cloneObject()
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
   * @see org.xmodel.IModelObject#createObject(java.lang.String)
   */
  public IModelObject createObject( String type)
  {
    return referent.createObject(  type);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getReferent()
   */
  public IModelObject getReferent()
  {
    return referent;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#revertUpdate(org.xmodel.memento.IMemento)
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
   * @see org.xmodel.IModelObject#restoreUpdate(org.xmodel.memento.IMemento)
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

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#setCachingPolicy(org.xmodel.external.ICachingPolicy)
   */
  @Override
  public void setCachingPolicy( ICachingPolicy cachingPolicy)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getCachingPolicy()
   */
  @Override
  public ICachingPolicy getCachingPolicy()
  {
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#setDirty(boolean)
   */
  @Override
  public void setDirty( boolean dirty)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#transaction()
   */
  @Override
  public ITransaction transaction()
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#clearCache()
   */
  @Override
  public void clearCache() throws CachingException
  {
    throw new UnsupportedOperationException();
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
  
  /**
   * Returns the native hash code of this object.
   * @return Returns the native hash code of this object.
   */
  public int nativeHashCode()
  {
    return super.hashCode();
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return "&"+referent;
  }
  
  private IModelObject parent;
  private IModelObject referent;
}
