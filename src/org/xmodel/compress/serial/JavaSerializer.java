package org.xmodel.compress.serial;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.xmodel.IModelObject;
import org.xmodel.compress.CompressorException;
import org.xmodel.compress.ISerializer;

/**
 * An implementation of ISerializer that uses Java serialization.
 */
public class JavaSerializer implements ISerializer
{
  /* (non-Javadoc)
   * @see org.xmodel.compress.ISerializer#readObject(java.io.DataInput)
   */
  @Override
  public Object readObject( DataInputStream input) throws IOException, ClassNotFoundException, CompressorException
  {
    int length = input.readInt();
    byte[] bytes = new byte[ length];
    input.readFully( bytes);
    
    ByteArrayInputStream byteIn = new ByteArrayInputStream( bytes);
    ObjectInputStream objectIn = new ObjectInputStream( byteIn);
    Object object = objectIn.readObject();
    objectIn.close();
    return object;
  }

  /* (non-Javadoc)
   * @see org.xmodel.compress.ISerializer#writeObject(java.io.DataOutput, java.lang.Object)
   */
  @Override
  public int writeObject( DataOutputStream output, IModelObject node) throws IOException, CompressorException
  {
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    ObjectOutputStream objectOut = new ObjectOutputStream( byteOut);
    objectOut.writeObject( node.getValue());
    objectOut.close();
    
    byte[] bytes = byteOut.toByteArray();
    output.writeInt( bytes.length);
    output.write( bytes);
    return bytes.length;
  }
}
