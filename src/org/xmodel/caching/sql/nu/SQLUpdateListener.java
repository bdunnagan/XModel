package org.xmodel.caching.sql.nu;

import org.xmodel.IModelObject;
import org.xmodel.external.NonSyncingListener;

public class SQLUpdateListener extends NonSyncingListener
{
  @Override
  public void notifyAddChild( IModelObject parent, IModelObject child, int index)
  {
    super.notifyAddChild( parent, child, index);
  }

  @Override
  public void notifyRemoveChild( IModelObject parent, IModelObject child, int index)
  {
    super.notifyRemoveChild( parent, child, index);
  }

  @Override
  public void notifyChange( IModelObject object, String attrName, Object newValue, Object oldValue)
  {
  }

  @Override
  public void notifyClear( IModelObject object, String attrName, Object oldValue)
  {
    notifyChange( object, attrName, null, null);
  }

  @Override
  public void notifyDirty( IModelObject object, boolean dirty)
  {
    // override default behavior
  }
  
  public boolean isEnabled()
  {
    return !disabled;
  }

  public void setEnabled( boolean enabled)
  {
    this.disabled = !enabled;
  }
  
  private boolean disabled;
}
