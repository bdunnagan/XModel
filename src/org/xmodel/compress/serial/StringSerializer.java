package org.xmodel.compress.serial;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.Charset;
import org.xmodel.compress.CompressorException;

/**
 * An implementation of ISerializer that calls the <code>toString</code> method to serialize to a string.
 * Any object class registered with this serializer will be deserialized to java.lang.String.
 */
public class StringSerializer extends AbstractSerializer
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
    return new String( bytes, charset);
  }

  /* (non-Javadoc)
   * @see org.xmodel.compress.ISerializer#writeObject(java.io.DataOutput, java.lang.Object)
   */
  @Override
  public int writeObject( DataOutput output, Object object) throws IOException, CompressorException
  {
    byte[] bytes = object.toString().getBytes( charset);
    output.writeInt( bytes.length);
    output.write( bytes);
    return bytes.length;
  }
  
  private final static Charset charset = Charset.forName( "UTF-8");
}
