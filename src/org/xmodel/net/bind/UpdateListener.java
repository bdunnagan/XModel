package org.xmodel.net.bind;

import java.io.IOException;
import org.jboss.netty.channel.Channel;
import org.xmodel.IModelObject;
import org.xmodel.external.ICachingPolicy;
import org.xmodel.external.IExternalReference;
import org.xmodel.external.NonSyncingListener;
import org.xmodel.log.SLog;
import org.xmodel.net.NetworkCachingPolicy;

class UpdateListener extends NonSyncingListener
{
  public UpdateListener( Channel channel, String query, IModelObject root)
  {
    this.channel = channel;
    this.query = query;
    this.root = root;
    this.enabled = true;
  }

  /**
   * Uninstall this listener.
   */
  public void uninstall()
  {
    uninstall( root);
  }
  
  /**
   * Enable/disable updates from this listener.
   * @param enabled True if updates should be sent.
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
    
    if ( !enabled) return;
    
    try
    {
      updateProtocol.sendAddChild( channel, parent, child, index);
    } 
    catch( IOException e)
    {
      SLog.warnf( this, "Failed to send add child update: %s", e.getMessage());
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.NonSyncingListener#notifyRemoveChild(org.xmodel.IModelObject, org.xmodel.IModelObject, int)
   */
  @Override
  public void notifyRemoveChild( IModelObject parent, IModelObject child, int index)
  {
    super.notifyRemoveChild( parent, child, index);
    
    if ( !enabled) return;
    
    try
    {
      updateProtocol.sendRemoveChild( channel, parent, index);
    } 
    catch( IOException e)
    {
      SLog.warnf( this, "Failed to send remove child update: %s", e.getMessage());
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.ModelListener#notifyChange(org.xmodel.IModelObject, java.lang.String, java.lang.Object, java.lang.Object)
   */
  @Override
  public void notifyChange( IModelObject object, String attrName, Object newValue, Object oldValue)
  {
    if ( !enabled) return;

    try
    {
      updateProtocol.sendChangeAttribute( channel, object, attrName, newValue);
    } 
    catch( IOException e)
    {
      SLog.warnf( this, "Failed to send change attribute update: %s", e.getMessage());
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.ModelListener#notifyClear(org.xmodel.IModelObject, java.lang.String, java.lang.Object)
   */
  @Override
  public void notifyClear( IModelObject object, String attrName, Object oldValue)
  {
    if ( !enabled) return;
    
    try
    {
      updateProtocol.sendClearAttribute( channel, object, attrName);
    } 
    catch( IOException e)
    {
      SLog.warnf( this, "Failed to send clear attribute update: %s", e.getMessage());
    }
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.ModelListener#notifyDirty(org.xmodel.IModelObject, boolean)
   */
  @Override
  public void notifyDirty( IModelObject object, boolean dirty)
  {
    //
    // Do not send notifications for network external references, otherwise the remote reference will
    // be marked not-dirty before a sync request is sent and the sync request will have no effect.
    //
    ICachingPolicy cachingPolicy = ((IExternalReference)object).getCachingPolicy();
    if ( cachingPolicy instanceof NetworkCachingPolicy) return;
    if ( cachingPolicy instanceof NetKeyCachingPolicy) return;
    
    if ( !enabled) return;
    
    try
    {
      updateProtocol.sendChangeDirty( channel, object, dirty);
    } 
    catch( IOException e)
    {
      SLog.warnf( this, "Failed to change dirty update: %s", e.getMessage());
    }
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals( Object object)
  {
    if ( object instanceof UpdateListener)
    {
      UpdateListener other = (UpdateListener)object;
      return other.channel == channel && other.query.equals( query);
    }
    return false;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode()
  {
    return channel.hashCode() + query.hashCode();
  }

  private UpdateProtocol updateProtocol;
  private Channel channel;
  private String query;
  private IModelObject root;
  private boolean enabled;
};
