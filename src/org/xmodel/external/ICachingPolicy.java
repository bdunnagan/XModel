/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ICachingPolicy.java
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

import java.net.URI;
import org.xmodel.IModelObject;
import org.xmodel.IModelObjectFactory;
import org.xmodel.xpath.expression.IExpression;

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
   * Set the factory.
   * @param factory The factory.
   */
  public void setFactory( IModelObjectFactory factory);
  
  /**
   * Returns the factory.
   * @return Returns the factory.
   */
  public IModelObjectFactory getFactory();
  
  /**
   * Define an additional caching stage for external references using this caching policy. The
   * first argument is evaluated relative to an external reference and identifies descendants
   * to be transformed into external references with the specified caching policy and initial
   * dirty state. Objects are transformed in the <code>insert</code> and <code>update</code>
   * methods. Note that objects are not automatically transformed by the <code>addChild</code>
   * method of the external reference. Therefore, the client must always use the 
   * <code>insert</code> and <code>update</code> methods to update the content of an external 
   * reference.
   * @param path An expression which identifies descendants of the external reference.
   * @param cachingPolicy The caching policy to be applied to identified descendants.
   * @param dirty The initial dirty state of identified descendants.
   */
  public void defineNextStage( IExpression path, ICachingPolicy cachingPolicy, boolean dirty);
  
  /**
   * Define an additional caching stage for external references using this caching policy. The
   * argument is cloned and added to an external reference when it is synchronized. The argument
   * is added after other children. Unlike the other <code>defineNextStage</code> method, this
   * method always adds the specified argument to the external reference.  In contrast the other
   * method transforms a child created during synchronization which matches an expression.  Note 
   * that the argument, itself, need not be an external reference.
   * @param stage The child to be added during synchronization.
   */
  public void defineNextStage( IModelObject stage);
  
  /**
   * Transform the specified subtree into an external reference tree. The argument is assumed to
   * be a prototype of an external reference which will be assigned this caching policy. External
   * references are created as necessary throughout the subtree according to the next stages 
   * defined on this caching policy.  Next stages are processed recursively. Note that this 
   * method destroys the argument subtree.
   * @param local The local subtree which does not contain external references.
   * @param dirty True if the root external reference should be marked dirty.
   * @param proto The prototype for external references.
   * @return Returns the transformed subtree.
   */
  public IExternalReference createExternalTree( IModelObject local, boolean dirty, IExternalReference proto);
  
  /**
   * Send a request to the external store to save the changes to the specified reference and unlock.
   * @param reference The reference to be unlocked and flushed.
   */
  public void checkin( IExternalReference reference);
  
  /**
   * Send a request to the external store to lock the specified reference for writing. When this
   * method returns, the reference should be locked and should be up-to-date with the content in the
   * external store.
   * @param reference The reference to be locked.
   */
  public void checkout( IExternalReference reference);

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

  /**
   * Returns the URI of the data-source for the specified reference. The URI schema is implementation
   * dependent and this contract does not guarantee that a method for resolving the URI exists. If the URI
   * can be resolved then it resolves to an XML stream which can be loaded by the <code>XmlIO</code> class.  
   * @param reference The reference.
   * @return Returns null or the URI of the reference data-source.
   * @throws CachingException When the implementation should return a non-null URI but cannot create it.
   */
  public URI getURI( IExternalReference reference) throws CachingException;
  
  /**
   * Create a string representation with the specified indentation.
   * @param indent The indentation (usually spaces).
   * @return Returns the string representation.
   */
  public String toString( String indent);
}
