package org.xmodel.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.xmodel.IAncestorListener;
import org.xmodel.IModel;
import org.xmodel.IModelListener;
import org.xmodel.IModelObject;
import org.xmodel.ModelListenerList;
import org.xmodel.PathListenerList;
import org.xmodel.memento.IMemento;

/**
 * An implementation of IModelObject that uses read/write locks to synchronize multi-threaded access.
 * This class is a synchronized wrapper for a delegate object.  It is not intended to be used in 
 * conjunction with the delegate.  It does not compare equal to the delegate or return the same hash
 * code as the delegate.  Instances of this class will be created for each descendant of the delegate
 * as necessary, with each descendant being uniquely associated with its own wrapper.
 */
public class ReadWriteElement implements IModelObject
{
  public ReadWriteElement( IModelObject delegate)
  {
    this.lock = new ReentrantReadWriteLock();
    this.delegate = delegate;
    if ( delegate.getParent() != null)
    {
      this.parent = new ReadWriteElement( delegate.getParent());
    }
  }
  
  protected ReadWriteElement( ReadWriteElement parent, IModelObject delegate)
  {
    this.lock = new ReentrantReadWriteLock();
    this.delegate = delegate;
    this.parent = parent;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#clearModel()
   */
  @Override
  public void clearModel()
  {
    writeLock();
    try
    {
      delegate.clearModel();
    }
    finally
    {
      writeUnlock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getModel()
   */
  @Override
  public IModel getModel()
  {
    readLock();
    try
    {
      return delegate.getModel();
    }
    finally
    {
      readUnlock();
    }
  }

  @Override
  public void setID( String id)
  {
    writeLock();
    try
    {
      delegate.setID( id);
    }
    finally
    {
      writeUnlock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getID()
   */
  @Override
  public String getID()
  {
    readLock();
    try
    {
      return delegate.getID();
    }
    finally
    {
      readUnlock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getType()
   */
  @Override
  public String getType()
  {
    readLock();
    try
    {
      return delegate.getType();
    }
    finally
    {
      readUnlock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#isType(java.lang.String)
   */
  @Override
  public boolean isType( String type)
  {
    readLock();
    try
    {
      return delegate.isType( type);
    }
    finally
    {
      readUnlock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#isDirty()
   */
  @Override
  public boolean isDirty()
  {
    readLock();
    try
    {
      return delegate.isDirty();
    }
    finally
    {
      readUnlock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#setAttribute(java.lang.String)
   */
  @Override
  public Object setAttribute( String attrName)
  {
    writeLock();
    try
    {
      return delegate.setAttribute( attrName);
    }
    finally
    {
      writeUnlock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#setAttribute(java.lang.String, java.lang.Object)
   */
  @Override
  public Object setAttribute( String attrName, Object attrValue)
  {
    writeLock();
    try
    {
      return delegate.setAttribute( attrName, attrValue);
    }
    finally
    {
      writeUnlock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getAttribute(java.lang.String)
   */
  @Override
  public Object getAttribute( String attrName)
  {
    readLock();
    try
    {
      return delegate.getAttribute( attrName);
    }
    finally
    {
      readUnlock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getAttributeNode(java.lang.String)
   */
  @Override
  public IModelObject getAttributeNode( String attrName)
  {
    readLock();
    try
    {
      return delegate.getAttributeNode( attrName);
    }
    finally
    {
      readUnlock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getAttributeNames()
   */
  @Override
  public Collection<String> getAttributeNames()
  {
    readLock();
    try
    {
      return delegate.getAttributeNames();
    }
    finally
    {
      readUnlock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#removeAttribute(java.lang.String)
   */
  @Override
  public Object removeAttribute( String attrName)
  {
    writeLock();
    try
    {
      return delegate.removeAttribute( attrName);
    }
    finally
    {
      writeUnlock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#setValue(java.lang.Object)
   */
  @Override
  public Object setValue( Object value)
  {
    writeLock();
    try
    {
      return delegate.setValue( value);
    }
    finally
    {
      writeUnlock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getValue()
   */
  @Override
  public Object getValue()
  {
    readLock();
    try
    {
      return delegate.getValue();
    }
    finally
    {
      readUnlock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getChildValue(java.lang.String)
   */
  @Override
  public Object getChildValue( String type)
  {
    readLock();
    try
    {
      return delegate.getChildValue( type);
    }
    finally
    {
      readUnlock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#addChild(org.xmodel.IModelObject)
   */
  @Override
  public void addChild( IModelObject object)
  {
    writeLock();
    try
    {
      createChildrenWrappers();
      
      if ( object instanceof ReadWriteElement)
      {
        ReadWriteElement rwObject = (ReadWriteElement)object;
        rwObject.parent = this;
        wrappers.put( rwObject.delegate, rwObject);
        delegate.addChild( rwObject.delegate);
      }
      else
      {
        wrappers.put( object, new ReadWriteElement( this, object));
        delegate.addChild( object);
      }
    }
    finally
    {
      writeUnlock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#addChild(org.xmodel.IModelObject, int)
   */
  @Override
  public void addChild( IModelObject object, int index)
  {
    writeLock();
    try
    {
      createChildrenWrappers();
      
      if ( object instanceof ReadWriteElement)
      {
        ReadWriteElement rwObject = (ReadWriteElement)object;
        rwObject.parent = this;
        wrappers.put( rwObject.delegate, rwObject);
        delegate.addChild( rwObject.delegate, index);
      }
      else
      {
        wrappers.put( object, new ReadWriteElement( this, object));
        delegate.addChild( object, index);
      }
    }
    finally
    {
      writeUnlock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#removeChild(int)
   */
  @Override
  public IModelObject removeChild( int index)
  {
    writeLock();
    try
    {
      createChildrenWrappers();
      
      IModelObject child = delegate.removeChild( index);
      ReadWriteElement wrapper = wrappers.remove( child);
      wrapper.parent = null;
      return wrapper;
    }
    finally
    {
      writeUnlock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#removeChild(org.xmodel.IModelObject)
   */
  @Override
  public void removeChild( IModelObject object)
  {
    writeLock();
    try
    {
      if ( object instanceof ReadWriteElement)
      {
        ReadWriteElement rwObject = (ReadWriteElement)object;
        int index = delegate.getChildren().indexOf( rwObject.delegate);
        if ( index >= 0) removeChild( index);
        rwObject.parent = null;
      }
      else
      {
        int index = delegate.getChildren().indexOf( object);
        if ( index >= 0) removeChild( index);
        if ( wrappers != null) wrappers.get( object).parent = null;
      }
    }
    finally
    {
      writeUnlock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#removeChildren()
   */
  @Override
  public void removeChildren()
  {
    writeLock();
    try
    {
      if ( wrappers != null)
      {
        for( IModelObject child: delegate.getChildren())
          wrappers.remove( child).parent = null;
      }
      
      delegate.removeChildren();
    }
    finally
    {
      writeUnlock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#removeChildren(java.lang.String)
   */
  @Override
  public void removeChildren( String type)
  {
    writeLock();
    try
    {
      if ( wrappers != null)
      {
        for( IModelObject child: delegate.getChildren( type))
          wrappers.remove( child).parent = null;
      }
      
      delegate.removeChildren( type);
    }
    finally
    {
      writeUnlock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#removeFromParent()
   */
  @Override
  public void removeFromParent()
  {
    readLock();
    try
    {
      if ( parent != null) parent.removeChild( this);
    }
    finally
    {
      readUnlock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getChild(int)
   */
  @Override
  public IModelObject getChild( int index)
  {
    readLock();
    try
    {
      createChildrenWrappers();
      return wrappers.get( index);
    }
    finally
    {
      readUnlock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getFirstChild(java.lang.String)
   */
  @Override
  public IModelObject getFirstChild( String type)
  {
    readLock();
    try
    {
      createChildrenWrappers();
      return wrappers.get( delegate.getFirstChild( type));
    }
    finally
    {
      readUnlock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getChild(java.lang.String, java.lang.String)
   */
  @Override
  public IModelObject getChild( String type, String name)
  {
    readLock();
    try
    {
      createChildrenWrappers();
      return wrappers.get( delegate.getChild( type, name));
    }
    finally
    {
      readUnlock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getCreateChild(java.lang.String)
   */
  @Override
  public IModelObject getCreateChild( String type)
  {
    writeLock();
    try
    {
      createChildrenWrappers();
      return wrappers.get( delegate.getCreateChild( type));
    }
    finally
    {
      writeUnlock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getCreateChild(java.lang.String, java.lang.String)
   */
  @Override
  public IModelObject getCreateChild( String type, String name)
  {
    writeLock();
    try
    {
      createChildrenWrappers();
      return wrappers.get( delegate.getCreateChild( type, name));
    }
    finally
    {
      writeUnlock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getChildren(java.lang.String, java.lang.String)
   */
  @Override
  public List<IModelObject> getChildren( String type, String name)
  {
    readLock();
    try
    {
      createChildrenWrappers();
      List<IModelObject> list = delegate.getChildren( type, name);
      List<IModelObject> result = new ArrayList<IModelObject>( list.size());
      for( IModelObject child: list) result.add( wrappers.get( child));
      return result;
    }
    finally
    {
      readUnlock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getChildren()
   */
  @Override
  public List<IModelObject> getChildren()
  {
    readLock();
    try
    {
      if ( wrappers == null) return Collections.emptyList();
      return new ArrayList<IModelObject>( wrappers.values());
    }
    finally
    {
      readUnlock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getChildren(java.lang.String)
   */
  @Override
  public List<IModelObject> getChildren( String type)
  {
    readLock();
    try
    {
      createChildrenWrappers();
      List<IModelObject> list = delegate.getChildren( type);
      List<IModelObject> result = new ArrayList<IModelObject>( list.size());
      for( IModelObject child: list) result.add( wrappers.get( child));
      return result;
    }
    finally
    {
      readUnlock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getTypesOfChildren()
   */
  @Override
  public Set<String> getTypesOfChildren()
  {
    readLock();
    try
    {
      createChildrenWrappers();
      return delegate.getTypesOfChildren();
    }
    finally
    {
      readUnlock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getNumberOfChildren()
   */
  @Override
  public int getNumberOfChildren()
  {
    readLock();
    try
    {
      createChildrenWrappers();
      return delegate.getNumberOfChildren();
    }
    finally
    {
      readUnlock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getNumberOfChildren(java.lang.String)
   */
  @Override
  public int getNumberOfChildren( String type)
  {
    readLock();
    try
    {
      createChildrenWrappers();
      return delegate.getNumberOfChildren( type);
    }
    finally
    {
      readUnlock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#internal_setParent(org.xmodel.IModelObject)
   */
  @Override
  public IModelObject internal_setParent( IModelObject parent)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#internal_addChild(org.xmodel.IModelObject, int)
   */
  @Override
  public void internal_addChild( IModelObject child, int index)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#internal_removeChild(int)
   */
  @Override
  public IModelObject internal_removeChild( int index)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#internal_notifyParent(org.xmodel.IModelObject, org.xmodel.IModelObject)
   */
  @Override
  public void internal_notifyParent( IModelObject newParent, IModelObject oldParent)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#internal_notifyAddChild(org.xmodel.IModelObject, int)
   */
  @Override
  public void internal_notifyAddChild( IModelObject child, int index)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#internal_notifyRemoveChild(org.xmodel.IModelObject, int)
   */
  @Override
  public void internal_notifyRemoveChild( IModelObject child, int index)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getParent()
   */
  @Override
  public IModelObject getParent()
  {
    readLock();
    try
    {
      return parent;
    }
    finally
    {
      readUnlock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getAncestor(java.lang.String)
   */
  @Override
  public IModelObject getAncestor( String type)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getRoot()
   */
  @Override
  public IModelObject getRoot()
  {
    readLock();
    try
    {
      IModelObject element = this;
      IModelObject parent = this.parent;
      while( parent != null)
      {
        element = parent;
        parent = parent.getParent();
      }
      return element;
    }
    finally
    {
      readUnlock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#addModelListener(org.xmodel.IModelListener)
   */
  @Override
  public void addModelListener( IModelListener listener)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#removeModelListener(org.xmodel.IModelListener)
   */
  @Override
  public void removeModelListener( IModelListener listener)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getModelListeners()
   */
  @Override
  public ModelListenerList getModelListeners()
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getPathListeners()
   */
  @Override
  public PathListenerList getPathListeners()
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#addAncestorListener(org.xmodel.IAncestorListener)
   */
  @Override
  public void addAncestorListener( IAncestorListener listener)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#removeAncestorListener(org.xmodel.IAncestorListener)
   */
  @Override
  public void removeAncestorListener( IAncestorListener listener)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#cloneObject()
   */
  @Override
  public IModelObject cloneObject()
  {
    readLock();
    try
    {
      return new ReadWriteElement( parent, delegate.cloneObject());
    }
    finally
    {
      readUnlock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#cloneTree()
   */
  @Override
  public IModelObject cloneTree()
  {
    readLock();
    try
    {
      return new ReadWriteElement( parent, delegate.cloneTree());
    }
    finally
    {
      readUnlock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#createObject(java.lang.String)
   */
  @Override
  public IModelObject createObject( String type)
  {
    readLock();
    try
    {
      return new ReadWriteElement( delegate.createObject( type));
    }
    finally
    {
      readUnlock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#getReferent()
   */
  @Override
  public IModelObject getReferent()
  {
    return this;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#revertUpdate(org.xmodel.memento.IMemento)
   */
  @Override
  public void revertUpdate( IMemento memento)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObject#restoreUpdate(org.xmodel.memento.IMemento)
   */
  @Override
  public void restoreUpdate( IMemento memento)
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Create a wrapper for each child of the delegate.
   */
  private synchronized final void createChildrenWrappers()
  {
    if ( wrappers == null)
    {
      wrappers = new LinkedHashMap<IModelObject, ReadWriteElement>();
      for( IModelObject child: delegate.getChildren())
        wrappers.put( child, new ReadWriteElement( this, child));
    }
  }
  
  /**
   * Acquire the read lock.
   */
  private final void readLock()
  {
    lock.readLock().lock();
  }

  /**
   * Release the read lock.
   */
  private final void readUnlock()
  {
    lock.readLock().unlock();
  }
  
  /**
   * Acquire the write lock.
   */
  private final void writeLock()
  {
    lock.writeLock().lock();
  }
  
  /**
   * Release the write lock.
   */
  private final void writeUnlock()
  {
    lock.writeLock().unlock();
  }

  private ReadWriteElement parent;
  private IModelObject delegate;
  private ReadWriteLock lock;
  private LinkedHashMap<IModelObject, ReadWriteElement> wrappers;
}
