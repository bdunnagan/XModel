package org.xmodel.compress.serial;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.xmodel.compress.CompressorException;

/**
 * An implementation of ISerializer that serializes java.lang.Boolean.
 */
public class BooleanSerializer extends AbstractSerializer
{
  /* (non-Javadoc)
   * @see org.xmodel.compress.ISerializer#readObject(java.io.DataInput)
   */
  @Override
  public Object readObject( DataInput input) throws IOException, ClassNotFoundException, CompressorException
  {
    return input.readByte();
  }

  /* (non-Javadoc)
   * @see org.xmodel.compress.ISerializer#writeObject(java.io.DataOutput, java.lang.Object)
   */
  @Override
  public int writeObject( DataOutput output, Object object) throws IOException, CompressorException
  {
    output.writeByte( (Boolean)object? 1 : 0);
    return 1;
  }
}
