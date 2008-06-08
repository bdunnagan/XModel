/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.external;

import dunnagan.bob.xmodel.IModelObject;

/**
 * An interface for managing the synchronization of IExternalReference objects with their
 * representation in external storage.
 */
public interface ICachingPolicy
{
  /**
   * Returns the cache or null.
   * @return Returns the cache or null.
   */
  public ICache getCache();
  
  /**
   * Send a request to the external store to lock the specified reference for writing. When this
   * method returns, the reference should be locked and should be up-to-date with the content in the
   * external store.
   * @param reference The reference to be locked.
   */
  public void checkout( IExternalReference reference);

  /**
   * Send a request to the external store to save the changes to the specified reference and unlock.
   * @param reference The reference to be unlocked and flushed.
   */
  public void checkin( IExternalReference reference);
  
  /**
   * Prepare the specified reference for use with this caching policy.
   * @param reference The reference.
   * @param dirty Whether the reference should be marked dirty.
   */
  public void prepare( IExternalReference reference, boolean dirty);
  
  /**
   * Synchronize the specified reference with its representation in the external store.
   * @param reference The reference to be synced.
   */
  public void sync( IExternalReference reference) throws CachingException;

  /**
   * Flush changes made to the specified reference to the external store.
   * @param reference The reference to be flushed.
   */
  public void flush( IExternalReference reference) throws CachingException;

  /**
   * Clear the cache for the specified reference and mark it dirty. If the specified reference has
   * listeners then it will be resynchronized before this method returns and the reference will not
   * be dirty.
   * @param reference The reference to be cleared.
   */
  public void clear( IExternalReference reference) throws CachingException;
  
  /**
   * Insert a new IExternalReference which has the content specified in the xml argument. If the
   * dirty argument is true then the content is assumed to be the partial reference schema and a
   * dirty reference is inserted into the tree.
   * @param parent The parent reference where the child will be added.
   * @param xml A string containing a well-formed xml document.
   * @param index The insertion index.
   * @param dirty True if a dirty reference should be inserted.
   */
  public void insert( IExternalReference parent, String xml, int index, boolean dirty) throws CachingException;

  /**
   * Insert a new IExternalReference which has the content in the specified subtree. If the dirty
   * argument is true then the content is assumed to be the partial reference schema and a dirty
   * reference is inserted into the tree.
   * @param parent The parent reference where the child will be added.
   * @param index The insertion index.
   * @param dirty True if a dirty reference should be inserted.
   * @param xml A string containing a well-formed xml document.
   */
  public void insert( IExternalReference parent, IModelObject object, int index, boolean dirty) throws CachingException;
  
  /**
   * Find the reference which correlates with the content in the specified xml argument and update it.
   * @param reference The reference to be updated.
   * @param xml A string containing a well-formed xml document.
   */
  public void update( IExternalReference reference, String xml) throws CachingException;

  /**
   * Find the reference which correlates with the content in the specified subtree and update it.
   * @param reference The reference to be updated.
   * @param element The root of the source subtree.
   */
  public void update( IExternalReference reference, IModelObject object) throws CachingException;
  
  /**
   * Find the reference which correlates with the content in the specified xml argument and remove it.
   * @param parent The parent reference where the child will be added.
   * @param xml A string containing a well-formed xml document.
   */
  public void remove( IExternalReference parent, String xml) throws CachingException;
  
  /**
   * Find the reference which correlates with the content in the specified subtree and remove it.
   * @param parent The parent reference where the child will be added.
   * @param element The root of the source subtree.
   */
  public void remove( IExternalReference parent, IModelObject object) throws CachingException;

  /**
   * Returns the names of attributes which can be accessed without syncing the reference.
   * @return Returns the names of attributes which can be accessed without syncing the reference.
   */
  public String[] getStaticAttributes();
  
  /**
   * Notify the ICachingPolicy that one or more attributes of the specified object have been
   * accessed for reading. This method is called before the read request is satisfied.
   * @param reference The reference which was accessed.
   * @param attrName The attribute being read or null if more than one.
   */
  public void readAttributeAccess( IExternalReference reference, String attrName);
  
  /**
   * Notify the ICachingPolicy that one or more children of the specified object have been accessed
   * for reading. This method is called before the read request is satisfied.
   * @param reference The reference which was accessed.
   */
  public void readChildrenAccess( IExternalReference reference);
  
  /**
   * Notify the ICachingPolicy that one or more attributes of the specified object have been
   * accessed for writing. This method is called before the write request is satisfied.
   * @param reference The reference which was accessed.
   * @param attrName The attribute being read or null if more than one.
   */
  public void writeAttributeAccess( IExternalReference reference, String attrName);
  
  /**
   * Notify the ICachingPolicy that one or more children of the specified object have been accessed
   * for writing. This method is called before the write request is satisfied.
   * @param reference The reference which was accessed.
   */
  public void writeChildrenAccess( IExternalReference reference);
}
