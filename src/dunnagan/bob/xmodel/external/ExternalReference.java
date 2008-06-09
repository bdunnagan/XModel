/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.external;

import dunnagan.bob.xmodel.IModel;
import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.ModelListenerList;
import dunnagan.bob.xmodel.ModelObject;

/**
 * TODO: These comments need to be updated.
 * An implementation of IExternalReference which will add itself to its ICachingPolicy cache when
 * the policy is installed and remove itself from the cache of the old policy if the policy is being
 * replaced. The reference is constructed with the dirty flag cleared. However, the dirty flag is
 * set when a valid ICachingPolicy is installed.
 * <p>
 * ExternalReference instances interact with the ICache of their associated ICachingPolicy according
 * to the following rules:
 * <ul>
 * <li>ExternalReferences are not initially added to the cache.
 * <li>An ExternalReference is removed from its cache when its caching policy is changed.
 * <li>An ExternalReference is added to its cache when it is parented.
 * <li>An ExternalReference is removed from its cache when it is unparented.
 * </ul>
 * Because of these semantics, ExternalReference instances which are not part of a model (do not have
 * a parent) are not managed by the ICache of their ICachingPolicy by default.
 * <p>
 * When an external reference is cleared, by calling <code>clearCache</code>, the reference will be
 * automatically resynced if it has a listener with a non-negative priority.
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
   * @see dunnagan.bob.xmodel.external.IExternalObject#getExternalReference()
   */
  public IExternalReference getExternalReference()
  {
    return this;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.IExternalReference#getParentReference()
   */
  public IExternalReference getParentReference()
  {
    IModelObject parent = getParent();
    while( parent != null)
    {
      if ( parent instanceof IExternalReference) return (IExternalReference)parent;
      parent = parent.getParent();
    }
    return null;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.IExternalReference#setCachingPolicy(dunnagan.bob.xmodel.external.ICachingPolicy)
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
   * @see dunnagan.bob.xmodel.external.IExternalReference#getStaticAttributes()
   */
  public String[] getStaticAttributes()
  {
    return getCachingPolicy().getStaticAttributes();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.IExternalReference#setDirty(boolean)
   */
  public void setDirty( boolean dirty)
  {
    boolean wasDirty = this.dirty;
    this.dirty = dirty;
    if ( wasDirty != dirty) notifyDirty( dirty);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.IExternalReference#isDirty()
   */
  public boolean isDirty()
  {
    return dirty;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.reference.IExternalObject#getCachingPolicy()
   */
  public ICachingPolicy getCachingPolicy()
  {
    return cachingPolicy;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.IExternalReference#sync()
   */
  public void sync() throws CachingException
  {
    ICachingPolicy cachingPolicy = getCachingPolicy();
    if ( cachingPolicy == null) throw new CachingException( "No caching policy to sync entity: "+this);
    setDirty( false);
    cachingPolicy.sync( this);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.IExternalReference#flush()
   */
  public void flush() throws CachingException
  {
    ICachingPolicy cachingPolicy = getCachingPolicy();
    if ( cachingPolicy == null) throw new CachingException( "No caching policy to flush entity: "+this);
    cachingPolicy.flush( this);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.ModelObject#readAttributeAccess(java.lang.String)
   */
  @Override
  protected void readAttributeAccess( String attrName)
  {
    if ( cachingPolicy != null) cachingPolicy.readAttributeAccess( this, attrName);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.ModelObject#readChildrenAccess()
   */
  @Override
  protected void readChildrenAccess()
  {
    if ( cachingPolicy != null) cachingPolicy.readChildrenAccess( this);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.ModelObject#writeAttributeAccess(java.lang.String)
   */
  @Override
  protected void writeAttributeAccess( String attrName)
  {
    if ( cachingPolicy != null) cachingPolicy.writeAttributeAccess( this, attrName);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.ModelObject#writeChildrenAccess()
   */
  @Override
  protected void writeChildrenAccess()
  {
    if ( cachingPolicy != null) cachingPolicy.writeChildrenAccess( this);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.IExternalReference#clearCache()
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
   * @see java.lang.Object#toString()
   */
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.IExternalReference#toString(java.lang.String)
   */
  public String toString( String indent)
  {
    IModel model = getModel();
    boolean wasSyncLocked = model.getSyncLock();
    model.setSyncLock( true);
    try
    {
      StringBuilder sb = new StringBuilder();
      sb.append( indent); sb.append( "&"); sb.append( super.toString()); sb.append( "\n");
      sb.append( getCachingPolicy().toString( indent+"  "));
      return sb.toString();
    }
    finally
    {
      model.setSyncLock( wasSyncLocked);
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.ModelObject#toString()
   */
  public String toString()
  {
    return toString( "");
  }

  private ICachingPolicy cachingPolicy;
  private boolean dirty;
}
