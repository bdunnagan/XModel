package org.xmodel.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.xmodel.IModel;
import org.xmodel.IModelObject;
import org.xmodel.ModelListenerList;
import org.xmodel.PathListenerList;
import org.xmodel.external.ICachingPolicy;

/**
 * An IStorageClass that caches an IModel instance, stores all data, and has a ModelListenerList instance.
 */
public final class ModelListenerAndCachingPolicyStorageClass implements IStorageClass
{
  /**
   * Copy the data from the specified storage class.
   * @param storageClass The storage class to be copied.
   */
  public ModelListenerAndCachingPolicyStorageClass( SmallDataStorageClass storageClass)
  {
    Statistics.decrement( storageClass);
    Statistics.increment( this);
    
    attributes = new HashMap<String, Object>();
    if ( storageClass.name1 != null) attributes.put( storageClass.name1, storageClass.value1);
    if ( storageClass.name2 != null) attributes.put( storageClass.name2, storageClass.value2);
    if ( storageClass.name3 != null) attributes.put( storageClass.name3, storageClass.value3);
    children = storageClass.children;
    modelListeners = new ModelListenerList();
  }
  
  /**
   * Copy the data from the specified storage class.
   * @param storageClass The storage class to be copied.
   */
  public ModelListenerAndCachingPolicyStorageClass( SmallDataCachingPolicyStorageClass storageClass)
  {
    Statistics.decrement( storageClass);
    Statistics.increment( this);
    
    attributes = new HashMap<String, Object>();
    if ( storageClass.name1 != null) attributes.put( storageClass.name1, storageClass.value1);
    if ( storageClass.name2 != null) attributes.put( storageClass.name2, storageClass.value2);
    if ( storageClass.name3 != null) attributes.put( storageClass.name3, storageClass.value3);
    children = storageClass.children;
    cachingPolicy = storageClass.cachingPolicy;
    dirty = storageClass.dirty;
    modelListeners = new ModelListenerList();
  }
  
  /**
   * Copy the data from the specified storage class.
   * @param storageClass The storage class to be copied.
   */
  public ModelListenerAndCachingPolicyStorageClass( DataAndCachingPolicyStorageClass storageClass)
  {
    Statistics.decrement( storageClass);
    Statistics.increment( this);
    
    attributes = storageClass.attributes;
    children = storageClass.children;
    cachingPolicy = storageClass.cachingPolicy;
    dirty = storageClass.dirty;
    modelListeners = new ModelListenerList();
  }
  
  /**
   * Copy the data from the specified storage class.
   * @param storageClass The storage class to be copied.
   */
  public ModelListenerAndCachingPolicyStorageClass( ModelListenerStorageClass storageClass)
  {
    Statistics.decrement( storageClass);
    Statistics.increment( this);
    
    model = storageClass.model;
    attributes = storageClass.attributes;
    children = storageClass.children;
    modelListeners = storageClass.modelListeners;
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
    return this;
  }

  /* (non-Javadoc)
   * @see org.xmodel.storage.IStorageClass#getModelListenersStorageClass()
   */
  @Override
  public IStorageClass getModelListenersStorageClass()
  {
    return this;
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
    this.model = model;
  }

  /* (non-Javadoc)
   * @see org.xmodel.storage.IStorageClass#getModel(org.xmodel.IModelObject)
   */
  @Override
  public IModel getModel()
  {
    return model;
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
    if ( attributes == null) attributes = new HashMap<String, Object>();
    return attributes.put( name, value);
  }

  /* (non-Javadoc)
   * @see org.xmodel.storage.IStorageClass#getAttribute(java.lang.String)
   */
  @Override
  public Object getAttribute( String name)
  {
    if ( attributes == null) return null;
    return attributes.get( name);
  }

  /* (non-Javadoc)
   * @see org.xmodel.storage.IStorageClass#getAttributeNames()
   */
  @Override
  public Collection<String> getAttributeNames()
  {
    return attributes.keySet();
  }

  /* (non-Javadoc)
   * @see org.xmodel.storage.IStorageClass#getModelListeners()
   */
  @Override
  public ModelListenerList getModelListeners()
  {
    return modelListeners;
  }

  /* (non-Javadoc)
   * @see org.xmodel.storage.IStorageClass#getPathListeners()
   */
  @Override
  public PathListenerList getPathListeners()
  {
    return null;
  }

  protected IModel model;
  protected Map<String, Object> attributes;
  protected List<IModelObject> children;
  protected ModelListenerList modelListeners;
  protected ICachingPolicy cachingPolicy;
  protected boolean dirty;
}
