package org.xmodel.compress.serial;

import java.io.File;
import java.io.IOException;
import org.jboss.netty.buffer.ChannelBuffer;
import org.xmodel.IModelObject;
import org.xmodel.compress.CompressorException;
import org.xmodel.compress.ISerializer;

/**
 * An implementation of ISerializer that serializes a java.io.File object.
 */
public class FileSerializer implements ISerializer
{
  /* (non-Javadoc)
   * @see org.xmodel.compress.ISerializer#readObject(java.io.DataInput)
   */
  @Override
  public Object readObject( ChannelBuffer input) throws IOException, ClassNotFoundException, CompressorException
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
  public int writeObject( ChannelBuffer output, IModelObject node) throws IOException, CompressorException
  {
    File file = (File)node.getValue();
    
    byte[] bytes = file.getPath().getBytes();
    int total = bytes.length;
    output.writeInt( bytes.length);
    output.writeBytes( bytes);
    
    if ( !file.isAbsolute())
    {
      String basePath = System.getProperty( "user.dir");
      bytes = basePath.getBytes();
      total += bytes.length;
      output.writeInt( bytes.length);
      output.writeBytes( bytes);
    }
    
    return total;
  }
}
