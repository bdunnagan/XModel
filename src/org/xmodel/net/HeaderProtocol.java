package org.xmodel.net;

import java.util.concurrent.atomic.AtomicLong;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

public class HeaderProtocol
{
  public enum Type
  {
    executeRequest,
    executeResponse,
    cancelRequest,
    bindRequest,
    bindResponse,
    unbindRequest,
    syncRequest,
    syncResponse,
    addChild,
    removeChild,
    changeAttribute,
    clearAttribute,
    changeDirty,
    register,
    unregister,
    echoRequest,
    echoResponse
  }
  
  /**
   * The header protocol optionally includes the correlation key for request/response pairs.
   * @param correlate True if correlation key should be included in header.
   */
  public HeaderProtocol( boolean includeCorrelation)
  {
    correlation = new AtomicLong( (int)System.nanoTime());
    this.includeCorrelation = includeCorrelation;
  }
  
  /**
   * Reset this instance by releasing internal resources.  This method should be called after 
   * the channel is closed to prevent conflict between protocol traffic and the freeing of resources.
   */
  public void reset()
  {
  }
  
  /**
   * @return Returns the next request correlation number.
   */
  public long correlation()
  {
    return correlation.getAndIncrement();
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
   * Read the correlation number from the buffer.
   * @param buffer The buffer.
   * @return Returns the correlation number.
   */
  public long readCorrelation( ChannelBuffer buffer)
  {
    if ( !includeCorrelation) throw new IllegalStateException( "Header does not include correlation.");
    return buffer.readLong();
  }
 
  /**
   * Write a message header and return the buffer.  The buffer returned by this method is sized 
   * to the length of the header plus the specified number of reserved bytes, allowing the content
   * of the message to be packed into the same buffer as the header.
   * @param reserve Extra space to reserve in header.
   * @param type The message type.
   * @param length The length of the message excluding header.
   * @return Returns the buffer containing the header.
   */
  public ChannelBuffer writeHeader( int reserve, Type type, long length)
  {
    ChannelBuffer buffer = ChannelBuffers.buffer( 9 + reserve);
    buffer.writeByte( type.ordinal());
    buffer.writeLong( reserve + length);
    return buffer;
  }
  
  /**
   * Write a message header and return the buffer.  The buffer returned by this method is sized 
   * to the length of the header plus the specified number of reserved bytes, allowing the content
   * of the message to be packed into the same buffer as the header.
   * @param reserve Extra space to reserve in header.
   * @param type The message type.
   * @param length The length of the message excluding header.
   * @param correlation The message correlation number.
   * @param reserve Extra space to reserve in header.
   * @return Returns the buffer containing the header.
   */
  public ChannelBuffer writeHeader( int reserve, Type type, long length, long correlation)
  {
    if ( includeCorrelation)
    {
      ChannelBuffer buffer = ChannelBuffers.buffer( 17 + reserve);
      buffer.writeByte( type.ordinal());
      buffer.writeLong( reserve + length);
      buffer.writeLong( correlation);
      return buffer;
    }
    else
    {
      return writeHeader( reserve, type, length);
    }
  }
  
  private AtomicLong correlation;
  private boolean includeCorrelation;
}  
