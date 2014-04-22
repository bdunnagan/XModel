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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xmodel.log.SLog;
import org.xmodel.util.HashMultiMap;
import org.xmodel.util.MultiMap;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.StatefulContext;

/**
 * Base implementation of IModel.
 */
public class Model implements IModel
{
  public Model()
  {
    updateStack = new ArrayList<Update>();
    updateObjects = new ArrayList<Update>();
    frozen = new ArrayList<IModelObject>();
    collections = new HashMultiMap<String, IModelObject>();

    // thread-access debugging
    if ( debugMap != null) debugMap.put( this, Thread.currentThread());
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IModel#getNamedContext(org.xmodel.xpath.expression.IContext, java.lang.String)
   */
  public IContext getNamedContext( IContext parent, String name)
  {
    if ( contexts == null) contexts = new HashMap<String, IContext>();
    
    IContext context = contexts.get( name);
    if ( context == null)
    {
      context = new StatefulContext( 
          parent,
          parent.getObject(),
          parent.getPosition(),
          parent.getSize());
      
      contexts.put( name, context);
    }
    
    return context;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModel#deleteContext(java.lang.String)
   */
  public void deleteContext( String name)
  {
    contexts.remove( name);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IModel#setThread(java.lang.Thread)
   */
  @Override
  public void setThread( Thread thread)
  {
    if ( debugMap != null) debugMap.put( this, thread);
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
  public void freeze( IModelObject object)
  {
    if ( object != null) frozen.add( object);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModel#unlock(org.xmodel.IModelObject)
   */
  public void unfreeze( IModelObject object)
  {
    if ( object != null) frozen.remove( object);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModel#isLocked(org.xmodel.IModelObject)
   */
  public IChangeSet isFrozen( IModelObject object)
  {
    if ( !frozen.contains( object)) return null;
    return getCurrentUpdate().getDeferredChangeSet();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModel#startUpdate()
   */
  public Update startUpdate()
  {
    if ( debugMap != null)
    {
      Thread currentThread = Thread.currentThread();
      Thread correctThread = debugMap.get( this);
      if ( correctThread != currentThread)
      {
        SLog.severef( this, "Thread access: model=%X, expected=%s", hashCode(), (correctThread != null)? correctThread.getName(): "");
        debugMap.put( this, currentThread);
      }
    }
    
    // current update cannot be in the reverted state
    int stackSize = updateStack.size();
    if ( stackSize > 0)
    {
      Update current = updateStack.get( stackSize-1);
      if ( current.isReverted())
      {
        SLog.severe( this, "Internal error: illegal update in reverted state!");
      }
    }
    
    // increase number of update objects if necessary
    int index = stackSize;
    if ( index >= updateObjects.size())
      updateObjects.add( new Update());
    
    // allocate update object
    Update update = updateObjects.get( index);
    update.setActive( true);
    update.clear();
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
    update.setActive( false);
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

  //private static Log log = Log.getLog( Model.class);
  
  private static final boolean debug = System.getProperty( "org.xmodel.Model.debug", null) != null;
  private static Map<Model, Thread> debugMap = debug? Collections.synchronizedMap( new Cache()): null;
  
  private static class Cache extends LinkedHashMap<Model, Thread>
  {
    private static final long serialVersionUID = 6237694180217498908L;

    public Cache()
    {
      super( 150, 0.75f, true);
    }
    
    /* (non-Javadoc)
     * @see java.util.LinkedHashMap#removeEldestEntry(java.util.Map.Entry)
     */
    @Override
    protected boolean removeEldestEntry( Map.Entry<Model, Thread> eldest) 
    {
      return size() > 100;
    }
  }

  private Map<String, IContext> contexts;
  private MultiMap<String, IModelObject> collections;
  private List<Update> updateStack;
  private List<Update> updateObjects;
  private List<IModelObject> frozen;
  private boolean syncLock;
  private boolean isReverted;
}
