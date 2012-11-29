package org.xmodel.storage;

import java.util.Collection;
import java.util.List;
import org.xmodel.IModel;
import org.xmodel.IModelObject;
import org.xmodel.ModelListenerList;
import org.xmodel.PathListenerList;
import org.xmodel.external.ICachingPolicy;

/**
 * An interface for interchangeable storage objects for IModelObject implementations.  Interchangeable
 * data storage allows model objects to dynamically adapt to storage demands while retaining their identity,
 * which reduces the size of data-models, and simplifies the implementation of many other aspects of the
 * framework.
 */
public interface IStorageClass
{
  /**
   * @param object The object that owns this storage class.
   * @return Returns null or an instance capable of caching an IModel instance.
   */
  public IStorageClass forModel( IModelObject object);
    
  /**
   * Set the model.
   * @param model The model.
   */
  public void setModel( IModel model);
  
  /**
   * @param object The object that owns this storage class.
   * @return Returns the model.
   */
  public IModel getModel( IModelObject object);
  
  /**
   * @return Returns null or an instance capable of storing caching policy data.
   */
  public IStorageClass forCachingPolicy();
  
  /**
   * Set the dirty flag.
   * @param dirty The dirty flag.
   */
  public void setDirty( boolean dirty);
  
  /**
   * @return Returns the dirty flag.
   */
  public boolean getDirty();
  
  /**
   * Set an instance of ICachingPolicy.
   * @param cachingPolicy The caching policy.
   */
  public void setCachingPolicy( ICachingPolicy cachingPolicy);
  
  /**
   * @return Returns null or the instance of ICachingPolicy.
   */
  public ICachingPolicy getCachingPolicy();
  
  /**
   * @return Returns null or an instance capable of storing children.
   */
  public IStorageClass forChildren();

  /**
   * @return Returns the list of children.
   */
  public List<IModelObject> getChildren();
  
  /**
   * @param attrName The name of the attribute.
   * @return Returns null or an instance capable of storing the specified attribute.
   */
  public IStorageClass forAttribute( String attrName);

  /**
   * Set an attribute on this object. If the attribute had a previous value, 
   * that value is returned.
   * @param attrName The name of the attribute.
   * @param attrValue The value of the attribute.
   * @return Returns null or the previous value of the attribute.
   */
  public Object setAttribute( String attrName, Object attrValue);
  
  /**
   * Get the value of an attribute by name.
   * @param attrName The name of the attribute.
   * @return Returns null or the value of the named attribute.
   */
  public Object getAttribute( String attrName);

  /**
   * Return a collection containing the names of all attributes on this object.
   * @return Returns the names of all the attributes.
   */
  public Collection<String> getAttributeNames();
  
  /**
   * @return Returns null or an instance capable of storing instances of IModelListener.
   */
  public IStorageClass forModelListeners();
  
  /**
   * @return Returns the storage for IModelListener instances.
   */
  public ModelListenerList getModelListeners();
  
  /**
   * @return Returns null or an instance capable of storing instances of IPathListener.
   */
  public IStorageClass forPathListeners();
  
  /**
   * @return Returns the storage for IPathListener instances.
   */
  public PathListenerList getPathListeners();
}
