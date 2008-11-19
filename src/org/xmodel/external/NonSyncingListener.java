/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.external;

import org.xmodel.IModelObject;
import org.xmodel.ModelListener;

/**
 * An IModelListener which fans-out into the subtree without syncing external references.
 * When external references are synced in some other way, however, the listener will fan-out.
 */
public class NonSyncingListener extends ModelListener
{
  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyParent(org.xmodel.IModelObject, 
   * org.xmodel.IModelObject, org.xmodel.IModelObject)
   */
  public void notifyParent( IModelObject child, IModelObject newParent, IModelObject oldParent)
  {
    if ( newParent == null) uninstall( child);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyAddChild(org.xmodel.IModelObject, 
   * org.xmodel.IModelObject, int)
   */
  public void notifyAddChild( IModelObject parent, IModelObject child, int index)
  {
    install( child);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyRemoveChild(org.xmodel.IModelObject, 
   * org.xmodel.IModelObject, int)
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
