/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.external;

import java.util.ArrayList;
import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.Xlate;


/**
 * An implementation of ICache which attempts to maintain an upper bound on the number of objects in the
 * cache.  The upper bound must be increased if an object is added to the cache and all the objects in 
 * the cache are locked.  An object is locked in the cache if it has at least one listener.  An object
 * is locked in the cache if it is the ancestor of an object in the cache.  When an object is cleared
 * from the cache, its ancestors will be unlocked.  This means that ancestors must use the same cache
 * instance as their descendants since they are locked and unlocked automatically.
 */
public class AccessOrderCache implements ICache
{
  public AccessOrderCache()
  {
    this( 0);
  }
  
  public AccessOrderCache( int capacity)
  {
    this.references = new ArrayList<IExternalReference>( capacity);
    this.capacity = capacity;
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICache#configure(org.xmodel.IModelObject)
   */
  public void configure( IModelObject annotation)
  {
    capacity = Xlate.get( annotation, "capacity", 0);
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICache#add(org.xmodel.external.IExternalReference)
   */
  public void add( IExternalReference reference)
  {
    // lock all the ancestors of the reference
    IModelObject parent = reference.getParent();
    while( parent != null) 
      if ( parent instanceof IExternalReference)
        memoryLock( (IExternalReference)parent);
    
    // remove eldest entry if capacity exceeded
    if ( references.size() >= capacity)
      clearEntry( references.remove( 0));
    
    // add reference to end of cache
    references.add( reference);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.external.ICache#remove(org.xmodel.external.IExternalReference)
   */
  public void remove( IExternalReference reference)
  {
    if ( references.remove( reference))
    {
      // unlock the first ancestor
      IModelObject parent = reference.getParent();
      while( parent != null)
      {
        if ( parent instanceof IExternalReference)
        {
          memoryUnlock( (IExternalReference)parent);
          break;
        }
      }
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICache#touch(org.xmodel.external.IExternalReference)
   */
  public void touch( IExternalReference reference)
  {
    references.remove( reference);
    references.add( reference);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.external.ICache#memoryLock(org.xmodel.external.IExternalReference)
   */
  public void memoryLock( IExternalReference object)
  {
    remove( object);
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICache#memoryUnlock(org.xmodel.external.IExternalReference)
   */
  public void memoryUnlock( IExternalReference object)
  {
    add( object);
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICache#size()
   */
  public int size()
  {
    return references.size();
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICache#capacity()
   */
  public int capacity()
  {
    return capacity;
  }

  /**
   * Clear the entry.
   * @param entry The entry.
   */
  protected void clearEntry( IExternalReference entry)
  {
    if ( entry.isDirty()) return; 
    try 
    { 
      entry.clearCache();
    }
    catch( CachingException e)
    {
      throw new IllegalStateException( "Attempt to purge cache entry which has listeners.");
    }
  }
  
  private int capacity;
  private List<IExternalReference> references;
}
