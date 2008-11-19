/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.xmodel.memento.IMemento;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.AttributeNode;


/**
 * An implementation of IModelObject which does not provide listener semantics. Although this implementation
 * is not thread-safe, a fragment containing Element instances may be operated on by different threads in 
 * sequence. 
 */
public class Element implements IModelObject
{
  /**
   * Create a ModelObject with the specified type.
   * @param type The type.
   */
  public Element( String type)
  {
    this.type = type.intern();
  }
  
  /**
   * Create a ModelObject with the specified type and id.
   * @param type The type.
   * @param id The id.
   */
  public Element( String type, String id)
  {
    this( type);
    setAttributeImpl( "id", id);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getModel()
   */
  public IModel getModel()
  {
    if ( model == null) model = ModelRegistry.getInstance().getModel();
    return model;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#setID(java.lang.String)
   */
  public void setID( String id)
  {
    writeAttributeAccess( "id");
    setAttribute( "id", id);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getID()
   */
  public String getID()
  {
    readAttributeAccess( "id");
    return Xlate.get( this, "id", "");
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
   * @see org.xmodel.IModelObject#isDirty()
   */
  public boolean isDirty()
  {
    return false;
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
    
    setAttributeImpl( attrName, attrValue);
    return oldValue;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getAttribute(java.lang.String)
   */
  public Object getAttribute( String attrName)
  {
    readAttributeAccess( attrName);
    
    if ( attributes == null) return null;
    return attributes.get( attrName);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getAttributeNode(java.lang.String)
   */
  public IModelObject getAttributeNode( String attrName)
  {
    if ( attributes != null && !attributes.containsKey( attrName)) return null;
    return new AttributeNode( attrName, this);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getAllAttributes()
   */
  public Collection<String> getAttributeNames()
  {
    readAttributeAccess( null);
    
    if ( attributes == null) return Collections.emptyList();
    return attributes.keySet();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#removeAttribute(java.lang.String)
   */
  public Object removeAttribute( String attrName)
  {
    writeAttributeAccess( attrName);
    
    Object oldValue = getAttribute( attrName);
    if ( oldValue == null) return null;
    
    removeAttributeImpl( attrName);
    return oldValue;
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
   * @see org.xmodel.IModelObject#addChild(org.xmodel.IModelObject)
   */
  public void addChild( IModelObject child)
  {
    int index = (children != null)? children.size(): 0;
    addChild( child, index);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#addChild(org.xmodel.IModelObject, int)
   */
  public void addChild( IModelObject child, int index)
  {
    if ( child == this) throw new IllegalArgumentException();
    
    writeChildrenAccess();
    
    IModelObject oldParent = child.getParent();
    if ( oldParent == this)
    {
      int oldIndex = getChildren().indexOf( child);

      // reposition child
      if ( removeChildImpl( oldIndex) != null && oldIndex < index) index--;
      addChildImpl( child, index);
    }
    else
    {
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
   * @see org.xmodel.IModelObject#removeChild(int)
   */
  public IModelObject removeChild( int index)
  {
    writeChildrenAccess();
    
    // bail if no children
    if ( children == null) return null;
    
    IModelObject child = children.get( index);
    if ( child != null)
    {
      removeChildImpl( index);
      child.internal_setParent( null);
    }
    
    return child;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#removeChild(org.xmodel.IModelObject)
   */
  public void removeChild( IModelObject child)
  {
    writeChildrenAccess();
    
    // bail if no children
    if ( children == null) return;
    
    int index = children.indexOf( child);
    if ( index >= 0)
    {
      removeChildImpl( index);
      child.internal_setParent( null);
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
   * @see org.xmodel.IModelObject#removeChildren()
   */
  public void removeChildren()
  {
    while( children != null && children.size() > 0)
      removeChild( children.size()-1);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#removeChildren(java.lang.String)
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
    readChildrenAccess();
    
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
   * @see org.xmodel.IModelObject#getChild(java.lang.String, java.lang.String)
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
   * @see org.xmodel.IModelObject#getCreateChild(java.lang.String)
   */
  public IModelObject getCreateChild( String type)
  {
    IModelObject child = getFirstChild( type);
    if ( child == null)
    {
      child = new Element( type);
      addChild( child);
    }
    return child;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getCreateChild(java.lang.String, java.lang.String)
   */
  public IModelObject getCreateChild( String type, String name)
  {
    IModelObject child = getChild( type, name);
    if ( child == null) 
    {
      child = new Element( type);
      child.setID( name);
      addChild( child);
    }
    return child;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getChildren(java.lang.String, java.lang.String)
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
   * @see org.xmodel.IModelObject#getChildren()
   */
  public List<IModelObject> getChildren()
  {
    readChildrenAccess();
    List<IModelObject> result = children;
    if ( children == null) result = Collections.emptyList();
    return result;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getChildren(java.lang.String)
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
   * @see org.xmodel.IModelObject#getTypesOfChildren()
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
   * @see org.xmodel.IModelObject#getNumberOfChildren()
   */
  public int getNumberOfChildren()
  {
    readChildrenAccess();
    return (children == null)? 0: children.size();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getNumberOfChildren(java.lang.String)
   */
  public int getNumberOfChildren( String type)
  {
    readChildrenAccess();
    return getChildren( type).size();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#internal_notifyParent(org.xmodel.IModelObject, org.xmodel.IModelObject)
   */
  public void internal_notifyParent( IModelObject newParent, IModelObject oldParent)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#internal_notifyAdd(org.xmodel.IModelObject, int)
   */
  public void internal_notifyAddChild( IModelObject child, int index)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#internal_notifyRemove(org.xmodel.IModelObject, int)
   */
  public void internal_notifyRemoveChild( IModelObject child, int index)
  {
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
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#removeListener(org.xmodel.IModelListener)
   */
  public void removeModelListener( IModelListener listener)
  {
    throw new UnsupportedOperationException();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getModelListeners()
   */
  public ModelListenerList getModelListeners()
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getPathListeners()
   */
  public PathListenerList getPathListeners()
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#addAncestorListener(org.xmodel.IAncestorListener)
   */
  public void addAncestorListener( IAncestorListener listener)
  {
    throw new UnsupportedOperationException();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#removeAncestorListener(org.xmodel.IAncestorListener)
   */
  public void removeAncestorListener( IAncestorListener listener)
  {
    throw new UnsupportedOperationException();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#cloneObject()
   */
  public IModelObject cloneObject()
  {
    return factory.createClone( this);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#cloneTree()
   */
  public IModelObject cloneTree()
  {
    return ModelAlgorithms.cloneTree( this, factory);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#createObject(java.lang.String)
   */
  public IModelObject createObject( String type)
  {
    return new Element( type);
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
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#restoreUpdate(org.xmodel.memento.IMemento)
   */
  public void restoreUpdate( IMemento iMemento)
  {
    throw new UnsupportedOperationException();
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
  
  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals( Object object)
  {
    if ( object instanceof Reference) return object.equals( this);
    return super.equals( object);
  }
  
  private final static IModelObjectFactory factory = new ElementFactory();

  private IModel model;
  private String type;
  private IModelObject parent;
  private List<IModelObject> children;
  private Map<String, Object> attributes;
}
