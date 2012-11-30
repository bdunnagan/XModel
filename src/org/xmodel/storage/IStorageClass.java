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
   * @return Returns true if this storage class stores an instance of IModel.
   */
  public boolean storesModel();
  
  /**
   * Set the model.
   * @param model The model.
   */
  public void setModel( IModel model);
  
  /**
   * @return Returns the model.
   */
  public IModel getModel();
  
  /**
   * @return Returns true if this storage class stores an instance of ICachingPolicy and dirty state.
   */
  public boolean storesCachingPolicy();
  
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
   * @return Returns true if this storage class stores children.
   */
  public boolean storesChildren();
  
  /**
   * @return Returns the list of children.
   */
  public List<IModelObject> getChildren();
  
  /**
   * @param name The name of the attribute.
   * @return Returns true if this storage class stores multiple attributes.
   */
  public boolean storesAttributes( String name);
  
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
   * @return Returns true if this storage class stores an instance of ModelListenerList.
   */
  public boolean storesModelListeners();
  
  /**
   * @return Returns the storage for IModelListener instances.
   */
  public ModelListenerList getModelListeners();
  
  /**
   * @return Returns true if this storage class stores an instance of PathListenerList.
   */
  public boolean storesPathListeners();
  
  /**
   * @return Returns the storage for IPathListener instances.
   */
  public PathListenerList getPathListeners();
}
