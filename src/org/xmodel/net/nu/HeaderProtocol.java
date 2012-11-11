package org.xmodel.net.nu;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.xmodel.net.nu.FullProtocolChannelHandler.Type;

public class HeaderProtocol
{
  /**
   * Reset this instance by releasing internal resources.  This method should be called after 
   * the channel is closed to prevent conflict between protocol traffic and the freeing of resources.
   */
  public void reset()
  {
  }
  
  /**
   * Read the message type of the next message in the specified buffer.
   * Fields must be read in order.
   * @param buffer The buffer.
   * @return Returns the message type.
   */
  public Type readType( ChannelBuffer buffer)
  {
    byte header = buffer.readByte();
    return Type.values()[ header & 0x1F];
  }
  
  /**
   * Read the length of the next message, excluding the header, in the specified buffer.
   * Fields must be read in order.
   * @param buffer The buffer.
   * @return Returns the length.
   */
  public long readLength( ChannelBuffer buffer)
  {
    return buffer.readLong();
  }
  
  /**
   * Write a message header and return the buffer.
   * @param type The message type.
   * @param length The length of the message excluding header.
   * @return Returns the buffer containing the header.
   */
  public ChannelBuffer writeHeader( Type type, long length)
  {
    ChannelBuffer buffer = ChannelBuffers.buffer( 32);
    buffer.writeByte( type.ordinal());
    buffer.writeLong( length);
    return buffer;
  }
}
