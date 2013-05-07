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

import org.xmodel.GlobalSettings;
import org.xmodel.IModel;
import org.xmodel.IModelObject;
import org.xmodel.ModelObject;
import org.xmodel.storage.CachingPolicyStorageClass;
import org.xmodel.storage.ValueStorageClass;

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
    super( new CachingPolicyStorageClass( new ValueStorageClass()), type);
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
    IModel model = GlobalSettings.getInstance().getModel();
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
