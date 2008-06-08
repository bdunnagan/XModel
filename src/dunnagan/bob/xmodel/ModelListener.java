/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel;

/**
 * An adapter for the IModelListener interface.
 */
public class ModelListener implements IModelListener
{
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelListener#notifyParent(dunnagan.bob.xmodel.IModelObject, 
   * dunnagan.bob.xmodel.IModelObject, dunnagan.bob.xmodel.IModelObject)
   */
  public void notifyParent( IModelObject child, IModelObject newParent, IModelObject oldParent)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelListener#notifyAdd(dunnagan.bob.xmodel.IModelObject, 
   * dunnagan.bob.xmodel.IModelObject, int)
   */
  public void notifyAddChild( IModelObject parent, IModelObject child, int index)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelListener#notifyRemove(dunnagan.bob.xmodel.IModelObject, 
   * dunnagan.bob.xmodel.IModelObject)
   */
  public void notifyRemoveChild( IModelObject parent, IModelObject child, int index)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelListener#notifyChange(dunnagan.bob.xmodel.IModelObject, 
   * java.lang.String, java.lang.String, java.lang.String)
   */
  public void notifyChange( IModelObject object, String attrName, Object newValue, Object oldValue)
  {
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelListener#notifyClear(dunnagan.bob.xmodel.IModelObject, 
   * java.lang.String, java.lang.String)
   */
  public void notifyClear( IModelObject object, String attrName, Object oldValue)
  {
  }
}
