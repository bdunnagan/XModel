/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ModelListenerList.java
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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * An implementation of IModelListener which contains IModelListener children and forwards listener
 * notifications to its children. Children can be added and removed during notification.
 */
public class ModelListenerList implements IModelListener
{
  /**
   * Add a listener.
   * @param listener The child listener. 
   */
  public void addListener( IModelListener listener)
  {
    if ( set == null) set = new HashMap<IModelListener, IModelListener>();
    set.put( listener, listener);
  }
  
  /**
   * Remove a child listener.
   * @param listener The child listener.
   */
  public void removeListener( IModelListener listener)
  {
    if ( set != null) set.remove( listener);
  }
  
  /**
   * Returns a list of the IModelListener instances.
   * @return Returns a list of the IModelListener instances.
   */
  public Set<IModelListener> getListeners()
  {
    return set.keySet();
  }

  /**
   * Returns the listener with the same identity as the specified listener.
   * @param listener The listener prototype.
   * @return Returns the listener with the same identity.
   */
  public IModelListener getIdentical( IModelListener listener)
  {
    return set.get( listener);
  }
  
  /**
   * Returns true if this list contains the specified IModelListener.
   * @param listener The listener being tested.
   * @return Returns true if this list contains the specified IModelListener.
   */
  public boolean contains( IModelListener listener)
  {
    return set.get( listener) != null;
  }
  
  /**
   * Returns the number of listeners.
   * @return Returns the number of listeners.
   */
  public int count()
  {
    return set.size();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyParent(
   * org.xmodel.IModelObject, org.xmodel.IModelObject, org.xmodel.IModelObject)
   */
  public void notifyParent( IModelObject child, IModelObject newParent, IModelObject oldParent)
  {
    // NOTE: the listeners have to be copied here because the set must be kept up-to-date elsewhere
    for( IModelListener listener: set.keySet().toArray( proto))
    {
      try
      {
        listener.notifyParent( child, newParent, oldParent);
      }
      catch( Exception e)
      {
        child.getModel().handleException( e);
      }
      finally
      {
        child.getModel().restore();
      }
    }
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyAdd(
   * org.xmodel.IModelObject, org.xmodel.IModelObject, int)
   */
  public void notifyAddChild( IModelObject parent, IModelObject child, int index)
  {
    // NOTE: the listeners have to be copied here because the set must be kept up-to-date elsewhere
    for( IModelListener listener: set.keySet().toArray( proto))
    {
      try
      {
        listener.notifyAddChild( parent, child, index);
      }
      catch( Exception e)
      {
        parent.getModel().handleException( e);
      }
      finally
      {
        parent.getModel().restore();
      }
    }        
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyRemove(
   * org.xmodel.IModelObject, org.xmodel.IModelObject)
   */
  public void notifyRemoveChild( IModelObject parent, IModelObject child, int index)
  {
    // NOTE: the listeners have to be copied here because the set must be kept up-to-date elsewhere
    for( IModelListener listener: set.keySet().toArray( proto))
    {
      try
      {
        listener.notifyRemoveChild( parent, child, index);
      }
      catch( Exception e)
      {
        parent.getModel().handleException( e);
      }
      finally
      {
        parent.getModel().restore();
      }
    }        
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyChange(
   * org.xmodel.IModelObject, java.lang.String, java.lang.String, java.lang.String)
   */
  public void notifyChange( IModelObject object, String attrName, Object newValue, Object oldValue)
  {
    // NOTE: the listeners have to be copied here because the set must be kept up-to-date elsewhere
    for( IModelListener listener: set.keySet().toArray( proto))
    {
      try
      {
        listener.notifyChange( object, attrName, newValue, oldValue);
      }
      catch( Exception e)
      {
        object.getModel().handleException( e);
      }
      finally
      {
        object.getModel().restore();
      }
    }
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyClear(
   * org.xmodel.IModelObject, java.lang.String, java.lang.String)
   */
  public void notifyClear( IModelObject object, String attrName, Object oldValue)
  {
    // NOTE: the listeners have to be copied here because the set must be kept up-to-date elsewhere
    for( IModelListener listener: set.keySet().toArray( proto))
    {
      try
      {
        listener.notifyClear( object, attrName, oldValue);
      }
      catch( Exception e)
      {
        object.getModel().handleException( e);
      }
      finally
      {
        object.getModel().restore();
      }
    }        
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyDirty(org.xmodel.IModelObject, boolean)
   */
  public void notifyDirty( IModelObject object, boolean dirty)
  {
    // NOTE: the listeners have to be copied here because the set must be kept up-to-date elsewhere
    for( IModelListener listener: set.keySet().toArray( proto))
    {
      try
      {
        listener.notifyDirty( object, dirty);
        
        // object was resynced by last notification so no further notification is necessary
        if ( object.isDirty() != dirty) break;
      }
      catch( Exception e)
      {
        object.getModel().handleException( e);
      }
      finally
      {
        object.getModel().restore();
      }
    }        
  }

//  /* (non-Javadoc)
//   * @see java.lang.Object#toString()
//   */
//  public String toString()
//  {
//    StringBuilder sb = new StringBuilder();
//
//    // sort by hashCode
//    List<IModelListener> listeners = new ArrayList<IModelListener>( set.size());
//    listeners.addAll( set.keySet());
//    Collections.sort( listeners, new Comparator<IModelListener>() {
//      public int compare( IModelListener o1, IModelListener o2)
//      {
//        if ( o1.hashCode() < o2.hashCode()) return -1;
//        if ( o1.hashCode() > o2.hashCode()) return 1;
//        return 0;
//      }
//    });
//    
//    // serialize
//    for( IModelListener listener: listeners)
//    {
//      sb.append( listener.getClass().getSimpleName());
//      sb.append( ", "); sb.append( listener.hashCode());
//      if ( listener instanceof LeafValueListener)
//      {
//        LeafValueListener cast = (LeafValueListener)listener;
//        sb.append( ", "); sb.append( cast.getExpression());
//        sb.append( ", "); sb.append( cast.getContext());
//      }
//      if ( listener instanceof IListenerChainLink)
//      {
//        IListenerChainLink cast = (IListenerChainLink)listener;
//        sb.append( ", "); sb.append( cast.getListenerChain()); 
//      }
//      sb.append( '\n');
//    }
//    
//    return sb.toString();
//  }

  private final static IModelListener[] proto = new IModelListener[ 0];
  private Map<IModelListener, IModelListener> set;
}
