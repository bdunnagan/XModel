/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.net;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import dunnagan.bob.xmodel.IModelListener;
import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.ModelListenerList;
import dunnagan.bob.xmodel.external.NonSyncingIterator;
import dunnagan.bob.xmodel.net.robust.ISession;

/**
 * A ModelListener installed by the Server to provide updates to the client. 
 * The listener automatically removes itself when the parent becomes null.
 */
public class ServerModelListener implements IModelListener
{
  /**
   * Create a listener which will notify the server of an update.
   * @param server The server.
   */
  protected ServerModelListener( ModelServer server, ISession session)
  {
    this.server = server;
    this.session = session;
    this.ignoreAttributes = new HashSet<String>();
    this.ignoreElements = new HashSet<IModelObject>();
    this.installSet = new HashSet<IModelObject>();
  }
  
  /**
   * Install this listener on the specified element and all of its ancestors. The general contract of this
   * listener installation is that an element which has this listener will also have this listener installed
   * on all of its ancestors.
   * @param element The element.
   */
  public void install( IModelObject element)
  {
    while( element != null)
    {
      element.addModelListener( this);
      installSet.add( element);
      element = element.getParent();
    }
  }
  
  /**
   * Uninstall this listener from all elements where it was installed.
   */
  public void uninstall()
  {
    for( IModelObject element: installSet)
      element.removeModelListener( this);
    installSet.clear();
  }
  
  /**
   * Remove this listener from the specified element, all of its descendants and any of its ancestors
   * which do not have a child which has this listener on it. The general contract of this listener 
   * installation is that an element which has this listener will also have this listener installed
   * on all of its ancestors.
   * @param element The element.
   */
  public void uninstall( IModelObject element)
  {
    // remove from element and descendants
    uninstallTree( element);
    
    // remove from ancestors which do not have a child with this listener installed
    IModelObject ancestor = element.getParent();
    while( ancestor != null)
    {
      for( IModelObject child: ancestor.getChildren())
      {
        ModelListenerList listeners = child.getModelListeners();
        if ( listeners != null && listeners.contains( this)) return;
      }
      
      ancestor.removeModelListener( this);
      installSet.remove( ancestor);
      
      ancestor = ancestor.getParent();
    }
  }

  /**
   * Install listener in subtree without syncing.
   * @param element The root of the subtree.
   */
  protected void installTree( IModelObject element)
  {
    NonSyncingIterator iterator = new NonSyncingIterator( element);
    while( iterator.hasNext())
    {
      IModelObject descendant = (IModelObject)iterator.next();
      descendant.addModelListener( this);
      installSet.add( descendant);
    }
  }
  
  /**
   * Uninstall listener in subtree without syncing.
   * @param element The root of the subtree.
   */
  private void uninstallTree( IModelObject element)
  {
    NonSyncingIterator iterator = new NonSyncingIterator( element);
    while( iterator.hasNext())
    {
      IModelObject descendant = (IModelObject)iterator.next();
      descendant.removeModelListener( this);
      installSet.remove( descendant);
    }
  }
  
  /**
   * Ignore updates to attributes with the specified name.
   * @param attrName The name of the attribute.
   */
  public void ignoreAttribute( String attrName)
  {
    ignoreAttributes.add( attrName);
  }
  
  /**
   * Report updates to attributes with the specified name.
   * @param attrName The name of the attribute.
   */
  public void regardAttribute( String attrName)
  {
    ignoreAttributes.remove( attrName);
  }
  
  /**
   * Ignore updates to the specified element.
   * @param object The object to be ignored or not.
   */
  public void ignoreElement( IModelObject object)
  {
    ignoreElements.add( object);
  }
  
  /**
   * Report updates to the specified element.
   * @param object The element.
   */
  public void regardElement( IModelObject object)
  {
    ignoreElements.remove( object);
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.ModelListener#notifyParent(dunnagan.bob.xmodel.IModelObject, dunnagan.bob.xmodel.IModelObject, 
   * dunnagan.bob.xmodel.IModelObject)
   */
  public void notifyParent( IModelObject child, IModelObject newParent, IModelObject oldParent)
  {
    if ( newParent != oldParent) child.removeModelListener( this);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.ModelListener#notifyAddChild(dunnagan.bob.xmodel.IModelObject, dunnagan.bob.xmodel.IModelObject, int)
   */
  public void notifyAddChild( IModelObject parent, IModelObject child, int index)
  {
    try
    {
      if ( !ignoreElements.contains( parent)) server.sendInsert( session, parent, child, index);
    }
    catch( Exception e)
    {
      e.printStackTrace( System.err);
      uninstall( parent);
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.ModelListener#notifyRemoveChild(dunnagan.bob.xmodel.IModelObject, dunnagan.bob.xmodel.IModelObject, int)
   */
  public void notifyRemoveChild( IModelObject parent, IModelObject child, int index)
  {
    try
    {
      uninstallTree( child);
      if ( !ignoreElements.contains( parent)) server.sendDelete( session, child);
    }
    catch( Exception e)
    {
      e.printStackTrace( System.err);
      uninstall( parent);
    }
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.ModelListener#notifyChange(dunnagan.bob.xmodel.IModelObject, java.lang.String, java.lang.Object, java.lang.Object)
   */
  public void notifyChange( IModelObject object, String attrName, Object newValue, Object oldValue)
  {
    try
    {
      if ( !ignoreAttributes.contains( attrName) && !ignoreElements.contains( object))
        server.sendUpdate( session, object);
    }
    catch( Exception e)
    {
      e.printStackTrace( System.err);
      uninstall( object);
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.ModelListener#notifyClear(dunnagan.bob.xmodel.IModelObject, java.lang.String, java.lang.Object)
   */
  public void notifyClear( IModelObject object, String attrName, Object oldValue)
  {
    try
    {
      if ( !ignoreAttributes.contains( attrName) && !ignoreElements.contains( object))
        server.sendUpdate( session, object);
    }
    catch( Exception e)
    {
      e.printStackTrace( System.err);
      uninstall( object);
    }
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode()
  {
    // hash code only needs to be unique by server since only one listener is installed per element
    return server.hashCode();
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals( Object object)
  {
    if ( object instanceof ServerModelListener)
    {
      ServerModelListener listener = (ServerModelListener)object;
      return listener.server == server && listener.session == session;
    }
    return false;
  }

  /**
   * Returns the listener for the specified server and session.
   * @param server The server.
   * @param session The session.
   * @return Returns the listener for the specified server and session.
   */
  public static ServerModelListener getInstance( ModelServer server, ISession session)
  {
    if ( map == null) map = new Hashtable<ISession, ServerModelListener>();
    ServerModelListener listener = map.get( session);
    if ( listener == null)
    {
      listener = new ServerModelListener( server, session);
      map.put( session, listener);
    }
    return listener;
  }
  
  private static Map<ISession, ServerModelListener> map;
  
  private ModelServer server;
  private ISession session;
  private Set<String> ignoreAttributes;
  private Set<IModelObject> ignoreElements;
  private Set<IModelObject> installSet;
}
