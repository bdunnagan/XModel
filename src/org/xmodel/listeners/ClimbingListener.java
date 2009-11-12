/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ClimbingListener.java
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
package org.xmodel.listeners;

import org.xmodel.IAncestorListener;
import org.xmodel.IModelObject;
import org.xmodel.ModelListener;

/**
 * This listener adds itself automatically to all the ancestors of a particular
 * domain object and provides notifications when ancestors of the domain object
 * are attached or detached from their parents via the ITreeListener interface.
 */
public class ClimbingListener extends ModelListener
{
  /**
   * Create a ClimbingListener that will notify the specified IAncestorListener.
   * @param listener The listener to be notified of ancestor events.
   */
  public ClimbingListener( IAncestorListener listener)
  {
    this.listener = listener;
  }

  /* (non-Javadoc)
   * @see org.xmodel.ITreeListener#addListenerToTree(org.xmodel.IModelObject)
   */
  public void addListenerToTree( IModelObject object)
  {
    this.object = object;
    while( object != null)
    {
      object.addModelListener( this);
      //System.out.println( "ClimbingListener added to "+object.getName());      
      object = object.getParent();
    }
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.ITreeListener#removeListenerFromTree(org.xmodel.IModelObject)
   */
  public void removeListenerFromTree( IModelObject object)
  {
    while( object != null)
    {
      object.removeModelListener( this);
      //System.out.println( "ClimbingListener removed from "+object.getName());      
      object = object.getParent();
    }
  }
    
  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyParent(org.xmodel.IModelObject, org.xmodel.IModelObject)
   */
  public void notifyParent( IModelObject child, IModelObject newParent, IModelObject oldParent)
  {
    // handle notification
    if ( newParent == null)
    {
      if ( oldParent == null) return;
      parentRemoved( child, oldParent);
    }
    else
    {
      if ( newParent == oldParent) return;
      if ( oldParent == null)
        parentAdded( child, newParent);
      else
        parentChanged( child, newParent, oldParent);
    }
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals( Object object)
  {
    if ( object instanceof ClimbingListener)
      return object == this;
    else if ( object instanceof IAncestorListener)
      return object == listener;
    else
      return false;
  }
  
  private final void parentAdded( IModelObject ancestor, IModelObject newParent)
  {
    addListenerToTree( newParent);
    listener.notifyAttach( object, ancestor, newParent, null);
  }

  private final void parentChanged( IModelObject ancestor, IModelObject newParent, IModelObject oldParent)
  {
    removeListenerFromTree( oldParent);
    addListenerToTree( newParent);
    listener.notifyAttach( object, ancestor, newParent, oldParent);
  }
  
  private final void parentRemoved( IModelObject ancestor, IModelObject oldParent)
  {
    removeListenerFromTree( oldParent);
    listener.notifyDetach( object, ancestor, oldParent);
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return "ClimbingListener( object="+object+")";
  }

  IModelObject object;
  IAncestorListener listener;
}
