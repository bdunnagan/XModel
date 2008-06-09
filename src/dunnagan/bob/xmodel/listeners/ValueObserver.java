/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.listeners;

import java.util.List;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.IPath;
import dunnagan.bob.xmodel.IPathListener;
import dunnagan.bob.xmodel.ModelListener;
import dunnagan.bob.xmodel.xpath.expression.IContext;

/**
 * When this listener is installed on a path, it provides notification for the value of each 
 * node in the node-set returned by the path. It also installs listeners on each node and 
 * provides notification when the value of the node changes.
 */
public abstract class ValueObserver extends ModelListener implements IPathListener
{
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.ModelListener#notifyChange(dunnagan.bob.xmodel.IModelObject, 
   * java.lang.String, java.lang.Object, java.lang.Object)
   */
  @Override
  public void notifyChange( IModelObject object, String attrName, Object newValue, Object oldValue)
  {
    if ( attrName.length() == 0) notifyValue( object, newValue, oldValue);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.ModelListener#notifyClear(dunnagan.bob.xmodel.IModelObject, 
   * java.lang.String, java.lang.Object)
   */
  @Override
  public void notifyClear( IModelObject object, String attrName, Object oldValue)
  {
    if ( attrName.length() == 0) notifyValue( object, null, oldValue);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.ModelListener#notifyDirty(dunnagan.bob.xmodel.IModelObject, boolean)
   */
  @Override
  public void notifyDirty( IModelObject object, boolean dirty)
  {
    // resync if necessary
    if ( dirty) object.getValue();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IPathListener#notifyAdd(dunnagan.bob.xmodel.xpath.expression.IContext, 
   * dunnagan.bob.xmodel.IPath, int, java.util.List)
   */
  public void notifyAdd( IContext context, IPath path, int pathIndex, List<IModelObject> nodes)
  {
    if ( pathIndex != path.length()) return;
    
    for( IModelObject object: nodes)
    {
      object.addModelListener( this);
      Object value = object.getValue();
      if ( value != null) notifyValue( object, value, null);
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IPathListener#notifyRemove(dunnagan.bob.xmodel.xpath.expression.IContext, 
   * dunnagan.bob.xmodel.IPath, int, java.util.List)
   */
  public void notifyRemove( IContext context, IPath path, int pathIndex, List<IModelObject> nodes)
  {
    if ( pathIndex != path.length()) return;
    
    for( IModelObject object: nodes)
      object.removeModelListener( this);
  }

  /**
   * Called once for each leaf of the path and again whenever the value of a leaf changes.
   * @param object A leaf of the path.
   * @param newValue The new value of the leaf.
   * @param oldValue The old value of the leaf.
   */
  protected abstract void notifyValue( IModelObject object, Object newValue, Object oldValue);
}
