/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.external;

import org.xmodel.IModel;
import org.xmodel.IModelObject;
import org.xmodel.ModelListenerList;
import org.xmodel.ModelObject;

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
    super( type);
    dirty = false;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.external.IExternalReference#setCachingPolicy(org.xmodel.external.ICachingPolicy)
   */
  public void setCachingPolicy( ICachingPolicy newCachingPolicy)
  {
    if ( cachingPolicy != null)
    {
      try { clearCache();} catch( CachingException e) {}
      ICache cache = cachingPolicy.getCache();
      if ( cache != null) cache.remove( this);
    }
    cachingPolicy = newCachingPolicy;
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.IExternalReference#getStaticAttributes()
   */
  public String[] getStaticAttributes()
  {
    return getCachingPolicy().getStaticAttributes();
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.IExternalReference#setDirty(boolean)
   */
  public void setDirty( boolean dirty)
  {
    this.dirty = dirty;
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.IExternalReference#isDirty()
   */
  public boolean isDirty()
  {
    return dirty;
  }

  /* (non-Javadoc)
   * @see org.xmodel.reference.IExternalObject#getCachingPolicy()
   */
  public ICachingPolicy getCachingPolicy()
  {
    return cachingPolicy;
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
   * @see org.xmodel.external.IExternalReference#flush()
   */
  public void flush() throws CachingException
  {
    ICachingPolicy cachingPolicy = getCachingPolicy();
    if ( cachingPolicy == null) throw new CachingException( "No caching policy to flush entity: "+this);
    cachingPolicy.flush( this);
  }

  /* (non-Javadoc)
   * @see org.xmodel.ModelObject#readAttributeAccess(java.lang.String)
   */
  @Override
  protected void readAttributeAccess( String attrName)
  {
    if ( cachingPolicy != null) cachingPolicy.readAttributeAccess( this, attrName);
  }

  /* (non-Javadoc)
   * @see org.xmodel.ModelObject#readChildrenAccess()
   */
  @Override
  protected void readChildrenAccess()
  {
    if ( cachingPolicy != null) cachingPolicy.readChildrenAccess( this);
  }

  /* (non-Javadoc)
   * @see org.xmodel.ModelObject#writeAttributeAccess(java.lang.String)
   */
  @Override
  protected void writeAttributeAccess( String attrName)
  {
    if ( cachingPolicy != null) cachingPolicy.writeAttributeAccess( this, attrName);
  }

  /* (non-Javadoc)
   * @see org.xmodel.ModelObject#writeChildrenAccess()
   */
  @Override
  protected void writeChildrenAccess()
  {
    if ( cachingPolicy != null) cachingPolicy.writeChildrenAccess( this);
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
   * @see java.lang.Object#toString()
   */
  /* (non-Javadoc)
   * @see org.xmodel.external.IExternalReference#toString(java.lang.String)
   */
  public String toString( String indent)
  {
    IModel model = getModel();
    boolean wasSyncLocked = model.getSyncLock();
    model.setSyncLock( true);
    try
    {
      StringBuilder sb = new StringBuilder();
      sb.append( indent); sb.append( "&"); sb.append( super.toString()); sb.append( " + ");
      sb.append( getCachingPolicy().toString( indent+"  "));
      return sb.toString();
    }
    finally
    {
      model.setSyncLock( wasSyncLocked);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.ModelObject#toString()
   */
  public String toString()
  {
    return toString( "");
  }

  private ICachingPolicy cachingPolicy;
  private boolean dirty;
}
