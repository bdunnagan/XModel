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
 * An IStorageClass that stores everything.
 */
public final class PathListenerAndCachingPolicyStorageClass implements IStorageClass
{
  /**
   * Copy the data from the specified storage class.
   * @param storageClass The storage class to be copied.
   */
  public PathListenerAndCachingPolicyStorageClass( PathListenerStorageClass storageClass)
  {
    model = storageClass.model;
    attributes = storageClass.attributes;
    children = storageClass.children;
    modelListeners = storageClass.modelListeners;
    pathListeners = storageClass.pathListeners;
  }
    
  /* (non-Javadoc)
   * @see org.xmodel.storage.IStorageClass#storesModel()
   */
  @Override
  public boolean storesModel()
  {
    return true;
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
   * @see org.xmodel.storage.IStorageClass#storesCachingPolicy()
   */
  @Override
  public boolean storesCachingPolicy()
  {
    return true;
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
   * @see org.xmodel.storage.IStorageClass#storesChildren()
   */
  @Override
  public boolean storesChildren()
  {
    return true;
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
   * @see org.xmodel.storage.IStorageClass#storesAttributes(java.lang.String)
   */
  @Override
  public boolean storesAttributes( String name)
  {
    return true;
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
   * @see org.xmodel.storage.IStorageClass#storesModelListeners()
   */
  @Override
  public boolean storesModelListeners()
  {
    return false;
  }

  /* (non-Javadoc)
   * @see org.xmodel.storage.IStorageClass#getModelListeners()
   */
  @Override
  public ModelListenerList getModelListeners()
  {
    if ( modelListeners == null) modelListeners = new ModelListenerList();
    return modelListeners;
  }

  /* (non-Javadoc)
   * @see org.xmodel.storage.IStorageClass#storesPathListeners()
   */
  @Override
  public boolean storesPathListeners()
  {
    return true;
  }

  /* (non-Javadoc)
   * @see org.xmodel.storage.IStorageClass#getPathListeners()
   */
  @Override
  public PathListenerList getPathListeners()
  {
    return pathListeners;
  }

  protected IModel model;
  protected Map<String, Object> attributes;
  protected List<IModelObject> children;
  protected ModelListenerList modelListeners;
  protected PathListenerList pathListeners;
  protected ICachingPolicy cachingPolicy;
  protected boolean dirty;
}
