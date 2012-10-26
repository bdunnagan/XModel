package org.xmodel.compress.serial;

import java.io.IOException;
import org.jboss.netty.buffer.ChannelBuffer;
import org.xmodel.compress.CompressorException;
import org.xmodel.compress.ISerializer;

/**
 * An implementation of ISerializer that calls the <code>toString</code> method to serialize to a string.
 * Any object class registered with this serializer will be deserialized to java.lang.String.
 */
public class StringSerializer implements ISerializer
{
  /* (non-Javadoc)
   * @see org.xmodel.compress.ISerializer#readObject(java.io.DataInput)
   */
  @Override
  public Object readObject( ChannelBuffer input) throws IOException, ClassNotFoundException, CompressorException
  {
    int length = input.readInt();
    byte[] bytes = new byte[ length];
    input.readBytes( bytes);
    return new String( bytes);
  }

  /* (non-Javadoc)
   * @see org.xmodel.compress.ISerializer#writeObject(java.io.DataOutput, java.lang.Object)
   */
  @Override
  public int writeObject( ChannelBuffer output, Object object) throws IOException, CompressorException
  {
    byte[] bytes = object.toString().getBytes();
    output.writeInt( bytes.length);
    output.writeBytes( bytes);
    return bytes.length;
  }
}
