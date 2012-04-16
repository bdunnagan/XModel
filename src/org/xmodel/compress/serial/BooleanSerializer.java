package org.xmodel.compress.serial;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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
  public Object readObject( DataInputStream input) throws IOException, ClassNotFoundException, CompressorException
  {
    return input.readBoolean();
  }

  /* (non-Javadoc)
   * @see org.xmodel.compress.ISerializer#writeObject(java.io.DataOutput, java.lang.Object)
   */
  @Override
  public int writeObject( DataOutputStream output, IModelObject node) throws IOException, CompressorException
  {
    output.writeBoolean( (Boolean)node.getValue());
    return 1;
  }
}
