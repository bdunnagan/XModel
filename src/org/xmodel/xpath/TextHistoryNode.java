/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * TextHistoryNode.java
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
import org.xmodel.memento.IMemento;


/**
 * An implementation of IModelObject which serves as a light-weight container for the text
 * attributes of other IModelObjects. The BasicConversion class stores the text node of an element
 * in an attribute with an empty attribute name. This container is used during the evaluation of
 * X-Path expressions to store the old value of the text node after it has been changed.
 */
public class TextHistoryNode implements INode
{
  /**
   * Create a TextNode to hold the text information.
   */
  public TextHistoryNode( Object oldValue)
  {
    this.oldValue = oldValue;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#clearModel()
   */
  @Override
  public void clearModel()
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getModel()
   */
  public IModel getModel()
  {
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getID()
   */
  public String getID()
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#setID(java.lang.String)
   */
  public void setID( String id)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#addAncestorListener(org.xmodel.IAncestorListener)
   */
  public void addAncestorListener( IAncestorListener listener)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#addChild(org.xmodel.IModelObject)
   */
  public void addChild( INode object)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#addChild(org.xmodel.IModelObject, int)
   */
  public void addChild( INode object, int index)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getChild(int)
   */
  public INode getChild( int index)
  {
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#removeChild(int)
   */
  public INode removeChild( int index)
  {
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#addModelListener(org.xmodel.IModelListener)
   */
  public void addModelListener( IModelListener listener)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#addModelListener(int, org.xmodel.IModelListener)
   */
  public void addModelListener( int priority, IModelListener listener)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#addPathListener(org.xmodel.IPath, org.xmodel.IPathListener)
   */
  public void addPathListener( IPath path, IPathListener listener)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#cloneObject()
   */
  public INode cloneObject()
  {
    return new TextHistoryNode( oldValue);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#cloneTree()
   */
  public INode cloneTree()
  {
    return cloneObject();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#createObject(java.lang.String)
   */
  public INode createObject( String type)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getReferent()
   */
  public INode getReferent()
  {
    return this;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getAncestor(java.lang.String)
   */
  public INode getAncestor( String type)
  {
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getAttribute(java.lang.String)
   */
  public Object getAttribute( String attrName)
  {
    return (attrName.length() == 0)? oldValue: null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getAttributeNode(java.lang.String)
   */
  public INode getAttributeNode( String attrName)
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
  public INode getChild( String type, String name)
  {
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getChildren()
   */
  public List<INode> getChildren()
  {
    return Collections.emptyList();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getChildren(java.lang.String, java.lang.String)
   */
  public List<INode> getChildren( String type, String name)
  {
    return Collections.emptyList();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getChildren(java.lang.String)
   */
  public List<INode> getChildren( String type)
  {
    return Collections.emptyList();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getValue()
   */
  public Object getValue()
  {
    return oldValue;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getValueNode()
   */
  public INode getValueNode()
  {
    throw new UnsupportedOperationException();
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
  public INode getCreateChild( String type, String name)
  {
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getCreateChild(java.lang.String)
   */
  public INode getCreateChild( String type)
  {
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getFirstChild(java.lang.String)
   */
  public INode getFirstChild( String type)
  {
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getModelListeners()
   */
  public ModelListenerList getModelListeners()
  {
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getPathListeners()
   */
  public PathListenerList getPathListeners()
  {
    return null;
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
  public INode getParent()
  {
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getRoot()
   */
  public INode getRoot()
  {
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getType()
   */
  public String getType()
  {
    return "text()";
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
  public INode internal_setParent( INode parent)
  {
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#internal_notifyParent(org.xmodel.IModelObject, org.xmodel.IModelObject)
   */
  public void internal_notifyParent( INode newParent, INode oldParent)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#internal_notifyAddChild(org.xmodel.IModelObject, int)
   */
  public void internal_notifyAddChild( INode child, int index)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#internal_notifyRemoveChild(org.xmodel.IModelObject, int)
   */
  public void internal_notifyRemoveChild( INode child, int index)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#internal_addChild(org.xmodel.IModelObject, int)
   */
  public void internal_addChild( INode child, int index)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#internal_removeChild(int)
   */
  public INode internal_removeChild( int index)
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
    return type.equals( "text()");
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#removeAncestorListener(org.xmodel.IAncestorListener)
   */
  public void removeAncestorListener( IAncestorListener listener)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#removeAttribute(java.lang.String)
   */
  public Object removeAttribute( String attrName)
  {
    if ( attrName.length() == 0) return oldValue;
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#removeChild(org.xmodel.IModelObject)
   */
  public void removeChild( INode object)
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
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#removeModelListener(org.xmodel.IModelListener)
   */
  public void removeModelListener( IModelListener listener)
  {
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
    if ( attrName.length() == 0) return oldValue;
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#setAttribute(java.lang.String)
   */
  public Object setAttribute( String attrName)
  {
    if ( attrName.length() == 0) return oldValue;
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#setValue(java.lang.Object)
   */
  public Object setValue( Object value)
  {
    return oldValue;
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
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals( Object object)
  {
    if ( object instanceof TextHistoryNode)
    {
      TextHistoryNode node = (TextHistoryNode)object;
      Object leftValue = getValue();
      Object rightValue = node.getValue();
      if ( leftValue == null || rightValue == null) return leftValue == rightValue;
      return leftValue.equals( rightValue);
    }
    return super.equals( object);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  public int hashCode()
  {
    return (oldValue == null)? 0: oldValue.hashCode()+1;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return (oldValue == null)? "": oldValue.toString();
  }

  Object oldValue;
}
