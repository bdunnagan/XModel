/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.external;

import dunnagan.bob.xmodel.IModelObject;

/**
 * An interface for the root of a subtree which is backed by an external storage location.
 * Synchronization of the subtree with the external content is controlled by an instance of
 * ICachingPolicy. IExternalReferences are associated with an ICache which, in conjunction with the
 * ICachingPolicy, manages the amount of data cached in memory. IExternalReferences are initially
 * dirty when constructed.
 */
public interface IExternalReference extends IModelObject
{
  /**
   * Return the nearest ancestor IExternalReference.
   * @return Return the nearest ancestor IExternalReference.
   */
  public IExternalReference getParentReference();
  
  /**
   * Set the ICachingPolicy for this IExternalReference and optionally mark the reference dirty. An
   * IExternalReference cannot be resolved until it has an ICachingPolicy. This method may be used
   * to enable and disable synchronization and flushing of the IExternalReference.
   * @param cachingPolicy The ICachingPolicy to associate with this reference.
   */
  public void setCachingPolicy( ICachingPolicy cachingPolicy, boolean dirty);
  
  /**
   * Returns the ICachingPolicy which controls the synchronization and flushing of this reference.
   * @return Returns the ICachingPolicy associated with this reference.
   */
  public ICachingPolicy getCachingPolicy();
 
  /**
   * Returns the names of attributes which can be accessed without syncing the reference.
   * @return Returns the names of attributes which can be accessed without syncing the reference.
   */
  public String[] getStaticAttributes();
  
  /**
   * Set whether this object is dirty or not.  This method should not be called after the object
   * has been sync'ed once.  The <code>clearCache</code> method should be used to transition
   * back to the dirty state.
   * @param dirty True if the object should be marked dirty.
   */
  public void setDirty( boolean dirty);
  
  /**
   * Returns true if this IExternalReference has never been accessed.
   * @return Returns true if this IExternalReference has never been accessed.
   */
  public boolean isDirty();
  
  /**
   * Returns true iff there is at least one IModelListener in the subtree.
   * @return Returns true iff there is at least one IModelListener in the subtree.
   */
  public boolean hasListeners();
  
  /**
   * Synchronize this IExternalReference using the installed ICachingPolicy. If no ICachingPolicy is
   * installed then this method will throw a CachingException. CachingExceptions may also be thrown
   * due to errors from IExternalStore.
   */
  public void sync() throws CachingException;

  /**
   * Flush this IExternalReference using the installed ICachingPolicy. If no ICachingPolicy is
   * installed then this method will throw a CachingException. CachingExceptions may also be thrown
   * due to errors from IExternalStore.
   */
  public void flush() throws CachingException;
  
  /**
   * If this reference is dirty then remove all children and mark dirty.  In addition, if this object
   * has listeners any where in its subtree as defined by the <code>hasListeners</code> method, then
   * the object will be immediately resync'ed.
   */
  public void clearCache() throws CachingException;
}
