/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel;

import java.util.*;

import dunnagan.bob.xmodel.path.IListenerChainLink;
import dunnagan.bob.xmodel.xpath.expression.LeafValueListener;

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
   * @see dunnagan.bob.xmodel.IModelListener#notifyParent(
   * dunnagan.bob.xmodel.IModelObject, dunnagan.bob.xmodel.IModelObject, dunnagan.bob.xmodel.IModelObject)
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
   * @see dunnagan.bob.xmodel.IModelListener#notifyAdd(
   * dunnagan.bob.xmodel.IModelObject, dunnagan.bob.xmodel.IModelObject, int)
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
   * @see dunnagan.bob.xmodel.IModelListener#notifyRemove(
   * dunnagan.bob.xmodel.IModelObject, dunnagan.bob.xmodel.IModelObject)
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
   * @see dunnagan.bob.xmodel.IModelListener#notifyChange(
   * dunnagan.bob.xmodel.IModelObject, java.lang.String, java.lang.String, java.lang.String)
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
   * @see dunnagan.bob.xmodel.IModelListener#notifyClear(
   * dunnagan.bob.xmodel.IModelObject, java.lang.String, java.lang.String)
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
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    StringBuilder sb = new StringBuilder();

    // sort by hashCode
    List<IModelListener> listeners = new ArrayList<IModelListener>( set.size());
    listeners.addAll( set.keySet());
    Collections.sort( listeners, new Comparator<IModelListener>() {
      public int compare( IModelListener o1, IModelListener o2)
      {
        if ( o1.hashCode() < o2.hashCode()) return -1;
        if ( o1.hashCode() > o2.hashCode()) return 1;
        return 0;
      }
    });
    
    // serialize
    for( IModelListener listener: listeners)
    {
      sb.append( listener.getClass().getSimpleName());
      sb.append( ", "); sb.append( listener.hashCode());
      if ( listener instanceof LeafValueListener)
      {
        LeafValueListener cast = (LeafValueListener)listener;
        sb.append( ", "); sb.append( cast.getExpression());
        sb.append( ", "); sb.append( cast.getContext());
      }
      if ( listener instanceof IListenerChainLink)
      {
        IListenerChainLink cast = (IListenerChainLink)listener;
        sb.append( ", "); sb.append( cast.getListenerChain()); 
      }
      sb.append( '\n');
    }
    
    return sb.toString();
  }

  private static IModelListener[] proto = new IModelListener[ 0];
  private Map<IModelListener, IModelListener> set;
}
