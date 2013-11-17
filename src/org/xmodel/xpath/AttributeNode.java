/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * AttributeNode.java
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
package org.xmodel.xpath;

import java.util.*;
import org.xmodel.*;
import org.xmodel.external.CachingException;
import org.xmodel.external.ICachingPolicy;
import org.xmodel.external.ITransaction;
import org.xmodel.memento.IMemento;
import org.xmodel.storage.IStorageClass;


/**
 * An implementation of IModelObject which serves as a light-weight container for attributes of
 * other IModelObjects. This container is used during the evaluation of X-Path expressions.
 */
public class AttributeNode implements IModelObject
{
  /**
   * Create an Attribute to hold the given attribute information. 
   * @param attrName The name of the attribute.
   * @param source The object where the attribute is stored.
   */
  public AttributeNode( String attrName, IModelObject source)
  {
    this.attrName = attrName;
    this.source = source;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#setStorageClass(org.xmodel.storage.IStorageClass)
   */
  @Override
  public void setStorageClass( IStorageClass storageClass)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getStorageClass()
   */
  @Override
  public IStorageClass getStorageClass()
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#addChild(org.xmodel.IModelObject)
   */
  public void addChild( IModelObject object)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#addChild(org.xmodel.IModelObject, int)
   */
  public void addChild( IModelObject object, int index)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getChild(int)
   */
  public IModelObject getChild( int index)
  {
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#removeChild(int)
   */
  public IModelObject removeChild( int index)
  {
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#addModelListener(org.xmodel.IModelListener)
   */
  public void addModelListener( IModelListener listener)
  {
    source.addModelListener( new AttributeListener( this, listener));
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#cloneObject()
   */
  public IModelObject cloneObject()
  {
    return new AttributeNode( attrName, source.cloneTree());
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#cloneTree()
   */
  public IModelObject cloneTree()
  {
    return cloneObject();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#createObject(java.lang.String)
   */
  public IModelObject createObject( String type)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getReferent()
   */
  public IModelObject getReferent()
  {
    return this;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getAncestor(java.lang.String)
   */
  public IModelObject getAncestor( String type)
  {
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getAttribute(java.lang.String)
   */
  public Object getAttribute( String attrName)
  {
    if ( attrName.length() == 0) return source.getAttribute( this.attrName);
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getAttributeNode(java.lang.String)
   */
  public IModelObject getAttributeNode( String attrName)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getAttributeNames()
   */
  public Collection<String> getAttributeNames()
  {
    List<String> names = new ArrayList<String>( 1);
    names.add( "");
    return names;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getChild(java.lang.String, java.lang.String)
   */
  public IModelObject getChild( String type, Object id)
  {
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getChildren()
   */
  public List<IModelObject> getChildren()
  {
    return Collections.emptyList();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getChildren(java.lang.String, java.lang.String)
   */
  public List<IModelObject> getChildren( String type, Object id)
  {
    return Collections.emptyList();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getChildren(java.lang.String)
   */
  public List<IModelObject> getChildren( String type)
  {
    return Collections.emptyList();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getValue()
   */
  public Object getValue()
  {
    return source.getAttribute( attrName);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getChildValue(java.lang.String)
   */
  public Object getChildValue( String type)
  {
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getCreateChild(java.lang.String, java.lang.String)
   */
  public IModelObject getCreateChild( String type, Object id)
  {
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getCreateChild(java.lang.String)
   */
  public IModelObject getCreateChild( String type)
  {
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getFirstChild(java.lang.String)
   */
  public IModelObject getFirstChild( String type)
  {
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getModelListeners()
   */
  public ModelListenerList getModelListeners()
  {
    ModelListenerList list = source.getModelListeners();
    if ( list == null) return null;
    
    ModelListenerList result = null;
    Set<IModelListener> listeners = list.getListeners();
    for( IModelListener listener: listeners)
    {
      if ( listener instanceof AttributeListener)
      {
        AttributeListener attributeListener = (AttributeListener)listener;
        if ( attributeListener.attrNode.equals( this))
        {
          if ( result == null) result = new ModelListenerList();
          result.addListener( attributeListener.listener);
        }
      }
    }

    return result;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getPathListeners()
   */
  public PathListenerList getPathListeners()
  {
    if ( pathListeners == null) pathListeners = new PathListenerList();
    return pathListeners;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getNumberOfChildren()
   */
  public int getNumberOfChildren()
  {
    return 0;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getNumberOfChildren(java.lang.String)
   */
  public int getNumberOfChildren( String type)
  {
    return 0;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getParent()
   */
  public IModelObject getParent()
  {
    return source;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getRoot()
   */
  public IModelObject getRoot()
  {
    return source.getRoot();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getType()
   */
  public String getType()
  {
    return attrName;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getTypesOfChildren()
   */
  public Set<String> getTypesOfChildren()
  {
    return Collections.emptySet();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#internal_setParent(org.xmodel.IModelObject)
   */
  public IModelObject internal_setParent( IModelObject parent)
  {
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#internal_notifyParent(org.xmodel.IModelObject, org.xmodel.IModelObject)
   */
  public void internal_notifyParent( IModelObject newParent, IModelObject oldParent)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#internal_notifyAddChild(org.xmodel.IModelObject, int)
   */
  public void internal_notifyAddChild( IModelObject child, int index)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#internal_notifyRemoveChild(org.xmodel.IModelObject, int)
   */
  public void internal_notifyRemoveChild( IModelObject child, int index)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#internal_addChild(org.xmodel.IModelObject, int)
   */
  public void internal_addChild( IModelObject child, int index)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#internal_removeChild(int)
   */
  public IModelObject internal_removeChild( int index)
  {
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#isDirty()
   */
  public boolean isDirty()
  {
    return false;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#isType(java.lang.String)
   */
  public boolean isType( String type)
  {
    return type.equals( attrName);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#removeAttribute(java.lang.String)
   */
  public Object removeAttribute( String attrName)
  {
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#removeChild(org.xmodel.IModelObject)
   */
  public void removeChild( IModelObject object)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#removeChildren()
   */
  public void removeChildren()
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#removeChildren(java.lang.String)
   */
  public void removeChildren( String type)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#removeFromParent()
   */
  public void removeFromParent()
  {
    source.removeAttribute( attrName);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#removeModelListener(org.xmodel.IModelListener)
   */
  public void removeModelListener( IModelListener listener)
  {
    source.removeModelListener( new AttributeListener( this, listener));
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#removePathListener(org.xmodel.IPath, org.xmodel.IPathListener)
   */
  public void removePathListener( IPath path, IPathListener listener)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#setAttribute(java.lang.String, java.lang.Object)
   */
  public Object setAttribute( String attrName, Object attrValue)
  {
    if ( attrName.length() == 0) return source.setAttribute( this.attrName, attrValue);
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#setAttribute(java.lang.String)
   */
  public Object setAttribute( String attrName)
  {
    if ( attrName.length() == 0) return setValue( "");
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#setValue(java.lang.Object)
   */
  public Object setValue( Object value)
  {
    return source.setAttribute( this.attrName, value);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#revertUpdate(org.xmodel.memento.IMemento)
   */
  public void revertUpdate( IMemento memento)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#restoreUpdate(org.xmodel.memento.IMemento)
   */
  public void restoreUpdate( IMemento memento)
  {
    throw new UnsupportedOperationException();
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
  
  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals( Object object)
  {
    if ( object instanceof AttributeNode)
    {
      AttributeNode node = (AttributeNode)object;
      IModelObject thisSource = source;
      IModelObject nodeSource = node.source;
      return attrName.equals( node.attrName) && thisSource.equals( nodeSource);
    }
    else if ( object instanceof AttributeHistoryNode)
    {
      AttributeHistoryNode node = (AttributeHistoryNode)object;
      return attrName.equals( node.attrName);
    }
    return super.equals( object);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  public int hashCode()
  {
    return attrName.hashCode() ^ source.hashCode();
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    Object attrValue = source.getAttribute( attrName);
    return (attrValue == null)? "": attrValue.toString();
  }

  /**
   * An intermediate listener for attribute changes of the source IModelObject, this listener
   * filters the events to those which are meaningful to listeners of this AttributeNode.
   */
  private class AttributeListener extends ModelListener
  {
    public AttributeListener( AttributeNode attrNode, IModelListener listener)
    {
      this.attrNode = attrNode;
      this.listener = listener;
    }
    
    /* (non-Javadoc)
     * @see org.xmodel.ModelListener#notifyChange(org.xmodel.IModelObject, java.lang.String, java.lang.Object, java.lang.Object)
     */
    public void notifyChange( IModelObject object, String attrName, Object newValue, Object oldValue)
    {
      if ( attrName.equals( attrNode.attrName))
        listener.notifyChange( attrNode, "", newValue, oldValue);
    }
  
    /* (non-Javadoc)
     * @see org.xmodel.ModelListener#notifyClear(org.xmodel.IModelObject, java.lang.String, java.lang.Object)
     */
    public void notifyClear( IModelObject object, String attrName, Object oldValue)
    {
      if ( attrName.equals( attrNode.attrName))
        listener.notifyClear( attrNode, "", oldValue);
    }
  
    /* (non-Javadoc)
     * @see org.xmodel.ModelListener#notifyDirty(org.xmodel.IModelObject, boolean)
     */
    public void notifyDirty( IModelObject object, boolean dirty)
    {
      // resync if necessary
      if ( dirty) object.getAttribute( attrNode.attrName);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
      return attrNode.hashCode() + listener.hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals( Object object)
    {
      if ( object instanceof AttributeListener)
      {
        AttributeListener other = (AttributeListener)object;
        return attrNode.equals( other.attrNode) && listener.equals( other.listener);
      }
      return super.equals( object);
    }

    AttributeNode attrNode;
    IModelListener listener;
  }
  
  String attrName;
  IModelObject source;
  PathListenerList pathListeners;
}
