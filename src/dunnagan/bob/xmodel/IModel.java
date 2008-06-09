/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel;

import java.net.URI;
import java.util.List;
import java.util.Set;


/**
 * An interface for managing the global state of a model. A model consists of one or more subtrees
 * which are managed within the context of a single thread. The XModel is not thread-safe. However,
 * different models can be accessed from different threads.  The global state of a model includes 
 * the set of indexes defined for the model and the current update stack.
 */
public interface IModel
{
  /**
   * Add a document to the specified collection.
   * @param collection The collection.
   * @param root The root node of the document.
   */
  public void addRoot( String collection, IModelObject root);
  
  /**
   * Remove a document from the specified collection.
   * @param collection The collection.
   * @param root The root node of the document.
   */
  public void removeRoot( String collection, IModelObject root);

  /**
   * Remove all documents from the specified collection.
   * @param collection The collection.
   */
  public void removeRoots( String collection);
  
  /**
   * Returns all the currently defined collections.
   * @return Returns all the currently defined collections.
   */
  public Set<String> getCollections();
  
  /**
   * Returns the models registered in the specified collection.
   * @param collection The collection.
   * @return Returns the models registered in the specified collection.
   */
  public List<IModelObject> getRoots( String collection);

  /**
   * Query the specified URI specification and return the matching element. This method calls
   * the <code>contains</code> method of each registered IExternalSpace implementation and uses
   * the first match to resolve the query.
   * @param uri The URI specification.
   * @return Returns the result of the query.
   */
  public List<IModelObject> query( URI uri);
  
  /**
   * Revert the current update (see Update for more information). This method may be called
   * even when there are no updates to be reverted.  In this case, it is a noop.
   */
  public void revert();

  /**
   * Restore the current update (see Update for more information). This method may be called
   * even when there are no updates to be reverted.  In this case, it is a noop.
   */
  public void restore();  
  
  /**
   * Lock the specified object so that its updates are deferred. This method is used by IModelObject
   * implementations to ensure that all listeners for an atomic update see the same state for the
   * one, two or three objects which are part of the atomic update. Locking should always be performed
   * around IModelListener notifications.  For convenience, this method does nothing with a null argument.
   * @param object The object whose state will be locked or null.
   */
  public void lock( IModelObject object);
  
  /**
   * Unlock the specified object. Updates will be processed when the <code>endUpdate</code> method is called.
   * @param object The object whose state will be unlocked.
   */
  public void unlock( IModelObject object);
  
  /**
   * Returns the IChangeSet which should be used to defer updates for the specified object
   * if the object has been locked by calling the <code>lock</code> method.  See 
   * <code>lock</code> for more information.
   * @param object The object being updated.
   * @return Returns null or the deferral change set.
   */
  public IChangeSet isLocked( IModelObject object);
  
  /**
   * Start a new update. When changes are being made to the model from listeners, it is 
   * possible for an update to be started before the previous update is finished. No single
   * object will ever have two started updates, however.
   */
  public Update startUpdate();
  
  /**
   * Returns the current update stack in order from oldest to most recent. 
   * @return Returns the update stack.
   */
  public List<Update> updateStack();
  
  /**
   * End the current update.
   */
  public void endUpdate();
  
  /**
   * Returns the current Update object.
   * @return Returns the current Update object.
   */
  public Update getCurrentUpdate();

  /**
   * Returns the <i>id</i> of the current update.
   * @return Returns the <i>id</i> of the current update.
   */
  public int getUpdateID();
  
  /**
   * Set the dispatcher which will execute a runnable in the thread associated with this model.
   * A dispatcher is required for automated, asynchronous updates to the model such as those
   * performed by the NetworkCachingPolicy.
   * @param dispatcher The dispatcher implementation.
   * @throws NullPointerException If no dispatcher is installed.
   */
  public void setDispatcher( IDispatcher dispatcher);
  
  /**
   * Returns the currently installed dispatcher.
   * @return Returns the currently installed dispatcher.
   */
  public IDispatcher getDispatcher();
  
  /**
   * Dispatch the specified runnable using the IDispatcher associated with this model.
   * @param runnable The runnable to be executed in the thread associated with this model.
   */
  public void dispatch( Runnable runnable);
  
  /**
   * Setting the sync lock temporarily prevents any external references from being synchronized.
   * @param lock True if the lock should be set.
   */
  public void setSyncLock( boolean lock);
  
  /**
   * Returns true if the sync lock is set.
   * @return Returns true if the sync lock is set.
   */
  public boolean getSyncLock();
  
  /**
   * Handle an exception or error which is thrown during a listener callback.
   * @parma e The exception.
   */
  public void handleException( Exception e);
}
