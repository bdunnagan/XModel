package org.xmodel.net.nu;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;

public class ErrorProtocol
{
  /**
   * Send an error message.
   * @param channel The channel.
   * @param message The message.
   */
  public void sendError( Channel channel, String message)
  {
    sendError( channel, 0, message);
  }
  
  /**
   * Send an error message.
   * @param channel The channel.
   * @param correlation The request correlation.
   * @param message The message.
   */
  public void sendError( Channel channel, int correlation, String message)
  {
  }
  
  /**
   * Handle an error message.
   * @param channel The channel.
   * @param buffer The buffer.
   */
  public void handleError( Channel channel, ChannelBuffer buffer)
  {
  }
}
