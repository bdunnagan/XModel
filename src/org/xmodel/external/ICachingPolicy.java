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
   * Synchronize the specified reference with its representation in the external store.
   * @param reference The reference to be synced.
   */
  public void sync( IExternalReference reference) throws CachingException;

  /**
   * @return Returns a new transaction.
   */
  public ITransaction transaction();
  
  /**
   * Clear the cache for the specified reference and mark it dirty. If the specified reference has
   * listeners then it will be resynchronized before this method returns and the reference will not
   * be dirty.
   * @param reference The reference to be cleared.
   */
  public void clear( IExternalReference reference) throws CachingException;

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
   * Find the reference which correlates with the content in the specified subtree and update it.
   * @param reference The reference to be updated.
   * @param element The root of the source subtree.
   */
  public void update( IExternalReference reference, IModelObject object) throws CachingException;
  
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
   * Called just before a request that accesses the attributes of the specified reference is fulfilled.
   * @param reference The reference that was accessed.
   * @param name The name of the attribute, or null if all attributes are being accessed.
   * @param write True if write access.
   */
  public void notifyAccessAttributes( IExternalReference reference, String name, boolean write);
  
  /**
   * Called just before a request that accesses the children of the specified reference is fulfilled.
   * @param reference The reference that was accessed.
   * @param write True if write access.
   */
  public void notifyAccessChildren( IExternalReference reference, boolean write);
  
  /**
   * Specify the names of attributes which should not cause synchronization. Two types of 
   * wildcards can be used. An asterisk by itself means <i>all attributes</i>. A prefix 
   * ending with a colon followed by an asterisk means <i>all attributes in namespace</i>.
   * @param attrNames An array of attribute names.
   */
  public void setStaticAttributes( String[] attrNames);
}
