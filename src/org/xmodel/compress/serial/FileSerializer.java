package org.xmodel.compress.serial;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import org.xmodel.compress.CompressorException;

/**
 * An implementation of ISerializer that serializes a java.io.File object.
 */
public class FileSerializer extends AbstractSerializer
{
  /* (non-Javadoc)
   * @see org.xmodel.compress.ISerializer#readObject(java.io.DataInput)
   */
  @Override
  public Object readObject( DataInput input) throws IOException, ClassNotFoundException, CompressorException
  {
    int length = input.readInt();
    byte[] bytes = new byte[ length];
    File file = new File( new String( bytes));
    if ( !file.isAbsolute())
    {
      length = input.readInt();
      bytes = new byte[ length];
      file = new File( new String( bytes), file.getPath());
    }
    return file;
  }

  /* (non-Javadoc)
   * @see org.xmodel.compress.ISerializer#writeObject(java.io.DataOutput, java.lang.Object)
   */
  @Override
  public int writeObject( DataOutput output, Object object) throws IOException, CompressorException
  {
    File file = (File)object;
    
    byte[] bytes = file.getPath().getBytes();
    int total = bytes.length;
    output.writeInt( bytes.length);
    output.write( bytes);
    
    if ( !file.isAbsolute())
    {
      String basePath = System.getProperty( "user.dir");
      bytes = basePath.getBytes();
      total += bytes.length;
      output.writeInt( bytes.length);
      output.write( bytes);
    }
    
    return total;
  }
}
