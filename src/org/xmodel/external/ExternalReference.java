/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ExternalReference.java
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
package org.xmodel.external;

import org.xmodel.IModel;
import org.xmodel.IModelObject;
import org.xmodel.ModelListenerList;
import org.xmodel.ModelObject;
import org.xmodel.storage.SmallDataCachingPolicyStorageClass;

/**
 * An implementation of IExternalReference which extends ModelObject to provide listener semantics.
 */
public class ExternalReference extends ModelObject implements IExternalReference
{  
  /**
   * Create the ExternalReference with the specified type which is not dirty.
   * @param type The type of this reference.
   */
  public ExternalReference( String type)
  {
    super( new SmallDataCachingPolicyStorageClass(), type);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.external.IExternalReference#setCachingPolicy(org.xmodel.external.ICachingPolicy)
   */
  public void setCachingPolicy( ICachingPolicy newCachingPolicy)
  {
    storageClass = storageClass.setCachingPolicyStorageClass();
    storageClass.setCachingPolicy( newCachingPolicy);
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.IExternalReference#setDirty(boolean)
   */
  public void setDirty( boolean dirty)
  {
    storageClass = storageClass.setCachingPolicyStorageClass();
    
    // 050109: added this back during xidget tree development
    boolean wasDirty = storageClass.getDirty();
    storageClass.setDirty( dirty);
    if ( wasDirty != dirty) 
    {
      notifyDirty( dirty);

      if ( dirty)
      {
        // resync immediately if reference has listeners
        ModelListenerList listeners = getModelListeners();
        if ( listeners != null && listeners.count() > 0) getChildren();
      }
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.IExternalReference#isDirty()
   */
  public boolean isDirty()
  {
    return storageClass.getDirty();
  }

  /* (non-Javadoc)
   * @see org.xmodel.reference.IExternalObject#getCachingPolicy()
   */
  public ICachingPolicy getCachingPolicy()
  {
    return storageClass.getCachingPolicy();
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.IExternalReference#sync()
   */
  public void sync() throws CachingException
  {
    ICachingPolicy cachingPolicy = getCachingPolicy();
    if ( cachingPolicy == null) throw new CachingException( "No caching policy to sync entity: "+this);
    setDirty( false);
    cachingPolicy.sync( this);
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.IExternalReference#transaction()
   */
  @Override
  public ITransaction transaction()
  {
    ICachingPolicy cachingPolicy = getCachingPolicy();
    if ( cachingPolicy == null) throw new CachingException( "No caching policy for this entity: "+this);
    return cachingPolicy.transaction();
  }

  /* (non-Javadoc)
   * @see org.xmodel.ModelObject#notifyAccessAttributes(java.lang.String, boolean)
   */
  @Override
  protected void notifyAccessAttributes( String name, boolean write)
  {
    ICachingPolicy cachingPolicy = storageClass.getCachingPolicy();
    if ( cachingPolicy != null) cachingPolicy.notifyAccessAttributes( this, name, write);
  }

  /* (non-Javadoc)
   * @see org.xmodel.ModelObject#notifyAccessChildren(boolean)
   */
  @Override
  protected void notifyAccessChildren( boolean write)
  {
    ICachingPolicy cachingPolicy = storageClass.getCachingPolicy();
    if ( cachingPolicy != null) cachingPolicy.notifyAccessChildren( this, write);
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.IExternalReference#clearCache()
   */
  public void clearCache() throws CachingException
  {
    ICachingPolicy cachingPolicy = getCachingPolicy();
    if ( cachingPolicy == null) throw new CachingException( "No caching policy to clear entity: "+this);
    cachingPolicy.clear( this);
  }
  
  /**
   * Notify listeners that the dirty state of a reference has changed.
   * @param reference The reference.
   * @param dirty The new dirty state.
   */
  protected void notifyDirty( boolean dirty)
  {
    ModelListenerList listeners = getModelListeners();
    if ( listeners != null) listeners.notifyDirty( this, dirty);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.ModelObject#createObject(java.lang.String)
   */
  @Override
  public IModelObject createObject( String type)
  {
    return new ExternalReference( type);
  }

  /* (non-Javadoc)
   * @see org.xmodel.ModelObject#toString()
   */
  public String toString()
  {
    IModel model = getModel();
    boolean wasSyncLocked = model.getSyncLock();
    model.setSyncLock( true);
    try
    {
      ICachingPolicy cachingPolicy = getCachingPolicy();
      StringBuilder sb = new StringBuilder();
      sb.append( "&"); sb.append( super.toString()); sb.append( " + ");
      if ( cachingPolicy != null) sb.append( cachingPolicy.toString());
      return sb.toString();
    }
    finally
    {
      model.setSyncLock( wasSyncLocked);
    }
  }
}
