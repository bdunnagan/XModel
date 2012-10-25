package org.xmodel.net.bind;

import org.jboss.netty.channel.Channel;
import org.xmodel.IModelObject;

public class UpdateProtocol
{
  public UpdateProtocol( BindCompressor compressor)
  {
    this.compressor = compressor;
  }
  
  /**
   * Send an update.
   * @param channel The channel.
   * @param parent The parent.
   * @param child The child.
   * @param index The insertion index.
   */
  public void sendAddChild( Channel channel, IModelObject parent, IModelObject child, int index)
  {
  }
  
  /**
   * Send an update.
   * @param channel The channel.
   * @param parent The parent.
   * @param index The index of the child.
   */
  public void sendRemoveChild( Channel channel, IModelObject parent, int index)
  {
  }

  /**
   * Send an update.
   * @param channel The channel.
   * @param element The element.
   * @param attrName The name of the attribute.
   * @param newValue The new value of the attribute.
   */
  public void sendChangeAttribute( Channel channel, IModelObject element, String attrName, Object newValue)
  {
  }

  /**
   * Send an update.
   * @param channel The channel.
   * @param element The element.
   * @param attrName The name of the attribute.
   */
  public void sendClearAttribute( Channel channel, IModelObject element, String attrName)
  {
  }

  /**
   * Send an update.
   * @param channel The channel.
   * @param element The element.
   * @param dirty The dirty state.
   */
  public void sendChangeDirty( Channel channel, IModelObject element, boolean dirty)
  {
  }
  
  private BindCompressor compressor;
}
