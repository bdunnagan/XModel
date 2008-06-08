/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.external;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.ModelListener;

/**
 * An IModelListener which fans-out into the subtree without syncing external references.
 * When external references are synced in some other way, however, the listener will fan-out.
 */
public class NonSyncingListener extends ModelListener
{
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelListener#notifyParent(dunnagan.bob.xmodel.IModelObject, 
   * dunnagan.bob.xmodel.IModelObject, dunnagan.bob.xmodel.IModelObject)
   */
  public void notifyParent( IModelObject child, IModelObject newParent, IModelObject oldParent)
  {
    if ( newParent == null) uninstall( child);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelListener#notifyAddChild(dunnagan.bob.xmodel.IModelObject, 
   * dunnagan.bob.xmodel.IModelObject, int)
   */
  public void notifyAddChild( IModelObject parent, IModelObject child, int index)
  {
    install( child);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelListener#notifyRemoveChild(dunnagan.bob.xmodel.IModelObject, 
   * dunnagan.bob.xmodel.IModelObject, int)
   */
  public void notifyRemoveChild( IModelObject parent, IModelObject child, int index)
  {
    uninstall( child);
  }

  /**
   * Install this listener in the specified element and descendants.
   * @param element The element.
   */
  public void install( IModelObject element)
  {
    NonSyncingIterator iter = new NonSyncingIterator( element);
    while ( iter.hasNext())
    {
      IModelObject object = (IModelObject)iter.next();
      object.addModelListener( this);
    }
  }

  /**
   * Remove this listener from the specified element and descendants.
   * @param element The element.
   */
  public void uninstall( IModelObject element)
  {
    NonSyncingIterator iter = new NonSyncingIterator( element);
    while ( iter.hasNext())
    {
      IModelObject object = (IModelObject)iter.next();
      object.removeModelListener( this);
    }
  }
}
