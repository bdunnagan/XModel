/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ServerModelListener.java
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
package org.xmodel.net;

import java.util.HashSet;
import java.util.Set;
import org.xmodel.IModelListener;
import org.xmodel.IModelObject;
import org.xmodel.external.NonSyncingIterator;
import org.xmodel.log.Log;
import org.xmodel.net.robust.IServerSession;


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
   * @see org.xmodel.ModelListener#notifyParent(org.xmodel.IModelObject, org.xmodel.IModelObject, 
   * org.xmodel.IModelObject)
   */
  public void notifyParent( IModelObject child, IModelObject newParent, IModelObject oldParent)
  {
    if ( newParent != oldParent) uninstall( child);
  }

  /* (non-Javadoc)
   * @see org.xmodel.ModelListener#notifyAddChild(org.xmodel.IModelObject, org.xmodel.IModelObject, int)
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
      log.exception( e);
      uninstall( parent);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.ModelListener#notifyRemoveChild(org.xmodel.IModelObject, org.xmodel.IModelObject, int)
   */
  public void notifyRemoveChild( IModelObject parent, IModelObject child, int index)
  {
    try
    {
      uninstall( child);
      if ( !ignoreElements.contains( parent)) 
        getServer().sendDelete( session, child);
    }
    catch( Exception e)
    {
      log.exception( e);
      uninstall( parent);
    }
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.ModelListener#notifyChange(org.xmodel.IModelObject, java.lang.String, java.lang.Object, java.lang.Object)
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
      log.exception( e);
      uninstall( object);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.ModelListener#notifyClear(org.xmodel.IModelObject, java.lang.String, java.lang.Object)
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
      log.exception( e);
      uninstall( object);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyDirty(org.xmodel.IModelObject, boolean)
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
    
  private static Log log = Log.getLog( "org.xmodel.net");
  
  private IServerSession session;
  private Set<String> ignoreAttributes;
  private Set<IModelObject> ignoreElements;
}
