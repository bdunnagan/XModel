package org.xmodel.compress.serial;

import java.io.IOException;
import org.jboss.netty.buffer.ChannelBuffer;
import org.xmodel.IModelObject;
import org.xmodel.compress.CompressorException;
import org.xmodel.compress.ISerializer;

/**
 * An implementation of ISerializer that serializes java.lang.Boolean.
 */
public class BooleanSerializer implements ISerializer
{
  /* (non-Javadoc)
   * @see org.xmodel.compress.ISerializer#readObject(java.io.DataInput)
   */
  @Override
  public Object readObject( ChannelBuffer input) throws IOException, ClassNotFoundException, CompressorException
  {
    return input.readByte();
  }

  /* (non-Javadoc)
   * @see org.xmodel.compress.ISerializer#writeObject(java.io.DataOutput, java.lang.Object)
   */
  @Override
  public int writeObject( ChannelBuffer output, IModelObject node) throws IOException, CompressorException
  {
    output.writeByte( (Boolean)node.getValue()? 1 : 0);
    return 1;
  }
}
