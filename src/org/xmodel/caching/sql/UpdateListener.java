package org.xmodel.caching.sql;

import org.xmodel.IModelObject;
import org.xmodel.external.NonSyncingListener;

class UpdateListener extends NonSyncingListener
{
  public UpdateListener()
  {
    this.enabled = true;
  }
  
  /**
   * Enable or disable notifications from this listener.
   * @param enabled True to enable.
   */
  public void setEnabled( boolean enabled)
  {
    this.enabled = enabled;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.external.NonSyncingListener#notifyAddChild(org.xmodel.IModelObject, org.xmodel.IModelObject, int)
   */
  @Override
  public void notifyAddChild( IModelObject parent, IModelObject child, int index)
  {
    super.notifyAddChild( parent, child, index);
    
    if ( enabled)
    {
      if ( isTable( parent))
      {
      }
      else if ( isRow( parent))
      {
      }
      else
      {
      }
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.NonSyncingListener#notifyRemoveChild(org.xmodel.IModelObject, org.xmodel.IModelObject, int)
   */
  @Override
  public void notifyRemoveChild( IModelObject parent, IModelObject child, int index)
  {
    super.notifyRemoveChild( parent, child, index);
    
    if ( enabled)
    {
      if ( isTable( parent))
      {
      }
      else if ( isRow( parent))
      {
      }
      else
      {
      }
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.ModelListener#notifyChange(org.xmodel.IModelObject, java.lang.String, java.lang.Object, java.lang.Object)
   */
  @Override
  public void notifyChange( IModelObject object, String attrName, Object newValue, Object oldValue)
  {
    if ( enabled)
    {
      if ( isTable( object))
      {
      }
      else if ( isRow( object))
      {
      }
      else if ( isRow( object.getParent()))
      {
      }
      else
      {
      }
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.ModelListener#notifyClear(org.xmodel.IModelObject, java.lang.String, java.lang.Object)
   */
  @Override
  public void notifyClear( IModelObject object, String attrName, Object oldValue)
  {
    if ( enabled)
    {
      if ( isTable( object))
      {
      }
      else if ( isRow( object))
      {
      }
      else if ( isRow( object.getParent()))
      {
      }
      else
      {
      }
    }
  }
  
  private boolean enabled;
  public List<String> ;
}