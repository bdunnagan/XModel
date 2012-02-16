/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * Model.java
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
package org.xmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.xmodel.log.Log;
import org.xmodel.util.HashMultiMap;
import org.xmodel.util.MultiMap;

/**
 * Base implementation of IModel.
 */
public class Model implements IModel
{
  public Model()
  {
    updateStack = new ArrayList<Update>();
    updateObjects = new ArrayList<Update>();
    locked = new ArrayList<IModelObject>();
    collections = new HashMultiMap<String, IModelObject>();

    // counter must start at one because 0 has meaning
    counter = 1;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IModelRegistry#addRoot(java.lang.String, org.xmodel.IModelObject)
   */
  public void addRoot( String collection, IModelObject root)
  {
    collections.put( collection, root);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelRegistry#removeRoot(java.lang.String, org.xmodel.IModelObject)
   */
  public void removeRoot( String collection, IModelObject root)
  {
    collections.remove( collection, root);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModel#removeRoots(java.lang.String)
   */
  public void removeRoots( String collection)
  {
    collections.removeAll( collection);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelRegistry#getRoots(java.lang.String)
   */
  public List<IModelObject> getRoots( String collection)
  {
    return collections.get( collection);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModel#getCollections()
   */
  public Set<String> getCollections()
  {
    return collections.keySet();
  }

  /**
   * Returns true if the specified path has a predicate.
   * @param path The path.
   * @return Returns true if the specified path has a predicate.
   */
  protected boolean hasPredicate( IPath path)
  {
    for( int i=0; i<path.length(); i++)
    {
      if ( path.getPathElement( i).predicate() != null)
        return true;
    }
    return false;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModel#revert()
   */
  public void revert()
  {
    // disable syncing while model is reverted (what would be the point?)
    setSyncLock( true);
    
    // there may not be a current update if this is an initial or final notification
    Update update = getCurrentUpdate();
    if ( update != null) update.revert();

    // flag it
    isReverted = true;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IModel#restoreUpdates()
   */
  public void restore()
  {
    for( Update update: updateStack) update.restore();
    
    // reenable syncing
    setSyncLock( false);
    
    // flag it
    isReverted = false;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IModel#isReverted()
   */
  @Override
  public boolean isReverted()
  {
    return isReverted;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModel#lock(org.xmodel.IModelObject)
   */
  public void lock( IModelObject object)
  {
    if ( object != null) locked.add( object);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModel#unlock(org.xmodel.IModelObject)
   */
  public void unlock( IModelObject object)
  {
    if ( object != null) locked.remove( object);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModel#unlock()
   */
  public void unlock()
  {
    locked.clear();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModel#isLocked(org.xmodel.IModelObject)
   */
  public IChangeSet isLocked( IModelObject object)
  {
    if ( !locked.contains( object)) return null;
    return getCurrentUpdate().getDeferredChangeSet();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModel#startUpdate()
   */
  public Update startUpdate()
  {
    // current update cannot be in the reverted state
    int stackSize = updateStack.size();
    if ( stackSize > 0)
    {
      Update current = updateStack.get( stackSize-1);
      if ( current.isReverted())
      {
        Throwable t = new Throwable();
        System.err.println( "Internal error: illegal update in reverted state: ");
        t.printStackTrace( System.err);
      }
    }
    
    // increase number of update objects if necessary
    int index = stackSize;
    if ( index >= updateObjects.size())
      updateObjects.add( new Update());
    
    // allocate update object
    Update update = updateObjects.get( index);
    update.clear();
    update.setId( counter++);
    updateStack.add( update);
    
    return update;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModel#updateStack()
   */
  public List<Update> updateStack()
  {
    return updateStack;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModel#endUpdate()
   */
  public void endUpdate()
  {
    int index = updateStack.size()-1;
    if ( index < 0) throw new IllegalStateException( "Update stack is empty.");
    Update update = updateStack.remove( index);
    update.clear();
    update.setId( 0);
    update.processDeferred();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IModel#getCurrentUpdate()
   */
  public Update getCurrentUpdate()
  {
    if ( updateStack.size() == 0) return null;
    return updateStack.get( updateStack.size() - 1);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModel#getUpdateID()
   */
  public int getUpdateID()
  {
    // return the current update id or the id of the last update
    Update update = getCurrentUpdate();
    if ( update != null) return update.getId();
    return counter-1;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModel#setDispatcher(org.xmodel.net.IDispatcher)
   */
  public void setDispatcher( IDispatcher dispatcher)
  {
    this.dispatcher = dispatcher;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModel#getDispatcher()
   */
  public IDispatcher getDispatcher()
  {
    return dispatcher;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModel#dispatch(java.lang.Runnable)
   */
  public void dispatch( Runnable runnable)
  {
    if ( dispatcher == null) 
      throw new IllegalStateException( "Dispatcher is not defined.");
    dispatcher.execute( runnable);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModel#setSyncLock(boolean)
   */
  public void setSyncLock( boolean syncLock)
  {
    this.syncLock = syncLock;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModel#getSyncLock()
   */
  public boolean getSyncLock()
  {
    return syncLock;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModel#handleException(java.lang.Exception)
   */
  public void handleException( Exception e)
  {
    log.exception( e);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IModel#setFeature(java.lang.Class, java.lang.Object)
   */
  @Override
  public <T> void setFeature( Class<?> feature, T implementation)
  {
    if ( features == null) features = new HashMap<Class<?>, Object>();
    features.put( feature, implementation);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModel#getFeature(java.lang.Class)
   */
  @SuppressWarnings("unchecked")
  @Override
  public <T> T getFeature( Class<T> clss)
  {
    if ( features != null) return (T)features.get( clss);
    return null;
  }

  private static Log log = Log.getLog( "org.xmodel");
  
  private MultiMap<String, IModelObject> collections;
  private List<Update> updateStack;
  private List<Update> updateObjects;
  private List<IModelObject> locked;
  private int counter;
  private IDispatcher dispatcher;
  private boolean syncLock;
  private Map<Class<?>, Object> features;
  private boolean isReverted;
}
