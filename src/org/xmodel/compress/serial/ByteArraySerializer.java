package org.xmodel.compress.serial;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.xmodel.compress.CompressorException;

/**
 * An implementation of ISerializer that serializes a byte array.
 * Any object class registered with this serializer will be deserialized to byte[].
 */
public class ByteArraySerializer extends AbstractSerializer
{
  /* (non-Javadoc)
   * @see org.xmodel.compress.ISerializer#readObject(java.io.DataInput)
   */
  @Override
  public Object readObject( DataInput input) throws IOException, ClassNotFoundException, CompressorException
  {
    int length = input.readInt();
    byte[] bytes = new byte[ length];
    input.readFully( bytes);
    return bytes;
  }

  /* (non-Javadoc)
   * @see org.xmodel.compress.ISerializer#writeObject(java.io.DataOutput, java.lang.Object)
   */
  @Override
  public int writeObject( DataOutput output, Object object) throws IOException, CompressorException
  {
    if ( object instanceof byte[])
    {
      byte[] bytes = (byte[])object;
      output.writeInt( bytes.length);
      output.write( bytes);
      return bytes.length;
    }
    else
    {
      throw new IllegalArgumentException( object.getClass().getName());
    }
  }
}
