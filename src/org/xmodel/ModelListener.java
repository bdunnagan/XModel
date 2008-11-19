/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel;

/**
 * An adapter for the IModelListener interface.
 */
public class ModelListener implements IModelListener
{
  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyParent(org.xmodel.IModelObject, 
   * org.xmodel.IModelObject, org.xmodel.IModelObject)
   */
  public void notifyParent( IModelObject child, IModelObject newParent, IModelObject oldParent)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyAdd(org.xmodel.IModelObject, 
   * org.xmodel.IModelObject, int)
   */
  public void notifyAddChild( IModelObject parent, IModelObject child, int index)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyRemove(org.xmodel.IModelObject, 
   * org.xmodel.IModelObject)
   */
  public void notifyRemoveChild( IModelObject parent, IModelObject child, int index)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyChange(org.xmodel.IModelObject, 
   * java.lang.String, java.lang.String, java.lang.String)
   */
  public void notifyChange( IModelObject object, String attrName, Object newValue, Object oldValue)
  {
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyClear(org.xmodel.IModelObject, 
   * java.lang.String, java.lang.String)
   */
  public void notifyClear( IModelObject object, String attrName, Object oldValue)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyDirty(org.xmodel.IModelObject, boolean)
   */
  public void notifyDirty( IModelObject object, boolean dirty)
  {
    // default behavior is to resync the object since generic listener is interested in everything
    if ( dirty) object.getChildren();
  }
}
