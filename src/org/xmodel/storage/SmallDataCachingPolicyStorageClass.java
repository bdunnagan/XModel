package org.xmodel.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.xmodel.IModel;
import org.xmodel.IModelObject;
import org.xmodel.ModelListenerList;
import org.xmodel.PathListenerList;
import org.xmodel.external.ICachingPolicy;

/**
 * An IStorageClass that stores a maximum of three attributes, children and an instance of ICachingPolicy and dirty flag.
 */
public final class SmallDataCachingPolicyStorageClass implements IStorageClass
{
  public SmallDataCachingPolicyStorageClass()
  {
    Statistics.increment( this);
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#finalize()
   */
  @Override
  protected void finalize() throws Throwable
  {
    super.finalize();
    Statistics.decrement( this);
  }

  /**
   * Copy the data from the specified storage class.
   * @param storageClass The storage class to be copied.
   */
  public SmallDataCachingPolicyStorageClass( ValueStorageClass storageClass)
  {
    Statistics.decrement( storageClass);
    Statistics.increment( this);
    
    if ( storageClass.value != null)
    {
      name1 = "";
      value1 = storageClass.value;
    }
  }
  
  /**
   * Copy the data from the specified storage class.
   * @param storageClass The storage class to be copied.
   */
  public SmallDataCachingPolicyStorageClass( SmallDataStorageClass storageClass)
  {
    Statistics.decrement( storageClass);
    Statistics.increment( this);
    
    name1 = storageClass.name1;
    value1 = storageClass.value1;
    name2 = storageClass.name2;
    value2 = storageClass.value2;
    name3 = storageClass.name3;
    value3 = storageClass.value3;
    children = storageClass.children;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.storage.IStorageClass#setCachingPolicyStorageClass()
   */
  @Override
  public IStorageClass setCachingPolicyStorageClass()
  {
    return this;
  }

  /* (non-Javadoc)
   * @see org.xmodel.storage.IStorageClass#getChildrenStorageClass()
   */
  @Override
  public IStorageClass getChildrenStorageClass()
  {
    return this;
  }

  /* (non-Javadoc)
   * @see org.xmodel.storage.IStorageClass#setAttributeStorageClass(java.lang.String)
   */
  @Override
  public IStorageClass setAttributeStorageClass( String name)
  {
    if ( name1 == null || name1.equals( name)) return this;
    if ( name2 == null || name2.equals( name)) return this;
    if ( name3 == null || name3.equals( name)) return this;
    return new DataAndCachingPolicyStorageClass( this);
  }

  /* (non-Javadoc)
   * @see org.xmodel.storage.IStorageClass#getModelListenersStorageClass()
   */
  @Override
  public IStorageClass getModelListenersStorageClass()
  {
    return new ModelListenerAndCachingPolicyStorageClass( this);
  }

  /* (non-Javadoc)
   * @see org.xmodel.storage.IStorageClass#getPathListenersStorageClass()
   */
  @Override
  public IStorageClass getPathListenersStorageClass()
  {
    return new PathListenerAndCachingPolicyStorageClass( this);
  }

  /* (non-Javadoc)
   * @see org.xmodel.storage.IStorageClass#setModel(org.xmodel.IModel)
   */
  @Override
  public void setModel( IModel model)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.storage.IStorageClass#getModel(org.xmodel.IModelObject)
   */
  @Override
  public IModel getModel()
  {
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.storage.IStorageClass#setDirty(boolean)
   */
  @Override
  public void setDirty( boolean dirty)
  {
    this.dirty = dirty;
  }

  /* (non-Javadoc)
   * @see org.xmodel.storage.IStorageClass#getDirty()
   */
  @Override
  public boolean getDirty()
  {
    return dirty;
  }

  /* (non-Javadoc)
   * @see org.xmodel.storage.IStorageClass#setCachingPolicy(org.xmodel.external.ICachingPolicy)
   */
  @Override
  public void setCachingPolicy( ICachingPolicy cachingPolicy)
  {
    this.cachingPolicy = cachingPolicy;
  }

  /* (non-Javadoc)
   * @see org.xmodel.storage.IStorageClass#getCachingPolicy()
   */
  @Override
  public ICachingPolicy getCachingPolicy()
  {
    return cachingPolicy;
  }

  /* (non-Javadoc)
   * @see org.xmodel.storage.IStorageClass#getChildren()
   */
  @Override
  public List<IModelObject> getChildren()
  {
    if ( children == null) children = new ArrayList<IModelObject>( 3);
    return children;
  }

  /* (non-Javadoc)
   * @see org.xmodel.storage.IStorageClass#setAttribute(java.lang.String, java.lang.Object)
   */
  @Override
  public Object setAttribute( String name, Object value)
  {
    if ( value != null)
    {
      // search for existing
      if ( name1 != null && name1.equals( name)) { Object old = value1; value1 = value; return old;}
      if ( name2 != null && name2.equals( name)) { Object old = value2; value2 = value; return old;}
      if ( name3 != null && name3.equals( name)) { Object old = value3; value3 = value; return old;}
      
      // search for space
      if ( name1 == null) { name1 = name; value1 = value; return null;}
      if ( name2 == null) { name2 = name; value2 = value; return null;}
      if ( name3 == null) { name3 = name; value3 = value; return null;}
    }
    else
    {
      if ( name1 != null && name1.equals( name)) { name1 = null; Object old = value1; value1 = value; return old;}
      if ( name2 != null && name2.equals( name)) { name2 = null; Object old = value2; value2 = value; return old;}
      if ( name3 != null && name3.equals( name)) { name3 = null; Object old = value3; value3 = value; return old;}
    }
    
    throw new IllegalStateException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.storage.IStorageClass#getAttribute(java.lang.String)
   */
  @Override
  public Object getAttribute( String name)
  {
    if ( name1 != null && name1.equals( name)) return value1;
    if ( name2 != null && name2.equals( name)) return value2;
    if ( name3 != null && name3.equals( name)) return value3;
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.storage.IStorageClass#getAttributeNames()
   */
  @Override
  public Collection<String> getAttributeNames()
  {
    List<String> names = new ArrayList<String>( 3);
    if ( name1 != null) names.add( name1);
    if ( name2 != null) names.add( name2);
    if ( name3 != null) names.add( name3);
    return names;
  }

  /* (non-Javadoc)
   * @see org.xmodel.storage.IStorageClass#getModelListeners()
   */
  @Override
  public ModelListenerList getModelListeners()
  {
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.storage.IStorageClass#getPathListeners()
   */
  @Override
  public PathListenerList getPathListeners()
  {
    return null;
  }
  
  protected String name1;
  protected Object value1;
  protected String name2;
  protected Object value2;
  protected String name3;
  protected Object value3;
  protected List<IModelObject> children;
  protected ICachingPolicy cachingPolicy;
  protected boolean dirty;
}
