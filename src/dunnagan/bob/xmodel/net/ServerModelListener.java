/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.net;

import java.util.HashSet;
import java.util.Set;

import dunnagan.bob.xmodel.IModelListener;
import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.external.NonSyncingIterator;
import dunnagan.bob.xmodel.net.robust.IServerSession;

/**
 * A ModelListener installed by the Server to provide updates to the client. 
 * The listener automatically removes itself when the parent becomes null.
 */
public class ServerModelListener implements IModelListener
{
  /**
   * Create a listener which will notify the server of an update.
   * @param session The session.
   */
  protected ServerModelListener( IServerSession session)
  {
    this.session = session;
    this.ignoreAttributes = new HashSet<String>();
    this.ignoreElements = new HashSet<IModelObject>();
  }

  /**
   * Install the listener in the tree of the element without syncing.
   * @param element The root of the tree.
   */
  private void install( IModelObject element)
  {
    // install listener throughout tree except on dirty references
    NonSyncingIterator iter = new NonSyncingIterator( element);
    while( iter.hasNext())
    {
      IModelObject node = iter.next();
      node.addModelListener( this);
    }
  }
  
  /**
   * Remove the listener from the specified element.
   */
  public void uninstall( IModelObject element)
  {
    NonSyncingIterator iter = new NonSyncingIterator( element);
    while( iter.hasNext())
    {
      IModelObject node = iter.next();
      node.removeModelListener( this);
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
    if ( newParent != oldParent) uninstall( child);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.ModelListener#notifyAddChild(dunnagan.bob.xmodel.IModelObject, dunnagan.bob.xmodel.IModelObject, int)
   */
  public void notifyAddChild( IModelObject parent, IModelObject child, int index)
  {
    try
    {
      install( child);
      if ( !ignoreElements.contains( parent)) getServer().sendInsert( session, parent, child, index);
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
      uninstall( child);
      if ( !ignoreElements.contains( parent)) getServer().sendDelete( session, child);
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
        getServer().sendChange( session, object, attrName, newValue);
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
        getServer().sendClear( session, object, attrName);
    }
    catch( Exception e)
    {
      e.printStackTrace( System.err);
      uninstall( object);
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelListener#notifyDirty(dunnagan.bob.xmodel.IModelObject, boolean)
   */
  public void notifyDirty( IModelObject object, boolean dirty)
  {
    // send dirty notification to client but never resync
    getServer().sendDirty( session, object, dirty);
  }

  /**
   * Returns the server.
   * @return Returns the server.
   */
  protected ModelServer getServer()
  {
    return (ModelServer)session.getServer();
  }
    
  private IServerSession session;
  private Set<String> ignoreAttributes;
  private Set<IModelObject> ignoreElements;
}
