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
 * An IStorageClass that stores a maximum of three attributes plus children.
 */
public final class SmallDataStorageClass implements IStorageClass
{
  /**
   * Copy the data from the specified storage class.
   * @param storageClass The storage class to be copied.
   */
  public SmallDataStorageClass( ValueStorageClass storageClass)
  {
    name1 = "";
    value1 = storageClass.value;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.storage.IStorageClass#setCachingPolicyStorageClass()
   */
  @Override
  public IStorageClass setCachingPolicyStorageClass()
  {
    return new SmallDataCachingPolicyStorageClass( this);
  }

  /* (non-Javadoc)
   * @see org.xmodel.storage.IStorageClass#getChildrenStorageClass()
   */
  @Override
  public IStorageClass getChildrenStorageClass()
  {
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.storage.IStorageClass#setAttributeStorageClass(java.lang.String)
   */
  @Override
  public IStorageClass setAttributeStorageClass( String name)
  {
    if ( name1 == null || name1.equals( name)) return null;
    if ( name2 == null || name2.equals( name)) return null;
    if ( name3 == null || name3.equals( name)) return null;
    return new DataStorageClass( this);
  }

  /* (non-Javadoc)
   * @see org.xmodel.storage.IStorageClass#getModelListenersStorageClass()
   */
  @Override
  public IStorageClass getModelListenersStorageClass()
  {
    return new SmallDataCachingPolicyStorageClass( this);
  }

  /* (non-Javadoc)
   * @see org.xmodel.storage.IStorageClass#getPathListenersStorageClass()
   */
  @Override
  public IStorageClass getPathListenersStorageClass()
  {
    return new SmallDataCachingPolicyStorageClass( this);
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
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.storage.IStorageClass#getDirty()
   */
  @Override
  public boolean getDirty()
  {
    return false;
  }

  /* (non-Javadoc)
   * @see org.xmodel.storage.IStorageClass#setCachingPolicy(org.xmodel.external.ICachingPolicy)
   */
  @Override
  public void setCachingPolicy( ICachingPolicy cachingPolicy)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.storage.IStorageClass#getCachingPolicy()
   */
  @Override
  public ICachingPolicy getCachingPolicy()
  {
    return null;
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
    if ( name1 == null || name1.equals( name)) { name1 = name; Object old = value1; value1 = value; return old;}
    if ( name2 == null || name2.equals( name)) { name2 = name; Object old = value2; value2 = value; return old;}
    if ( name3 == null || name3.equals( name)) { name3 = name; Object old = value3; value3 = value; return old;}
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.storage.IStorageClass#getAttribute(java.lang.String)
   */
  @Override
  public Object getAttribute( String name)
  {
    if ( name1 != null || name1.equals( name)) return value1;
    if ( name2 != null || name2.equals( name)) return value2;
    if ( name3 != null || name3.equals( name)) return value3;
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
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.storage.IStorageClass#getPathListeners()
   */
  @Override
  public PathListenerList getPathListeners()
  {
    throw new UnsupportedOperationException();
  }
  
  protected String name1;
  protected Object value1;
  protected String name2;
  protected Object value2;
  protected String name3;
  protected Object value3;
  protected List<IModelObject> children;
}
