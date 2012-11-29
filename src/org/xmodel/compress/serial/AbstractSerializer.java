package org.xmodel.compress.serial;

import java.io.DataOutput;
import java.io.IOException;
import org.xmodel.IModelObject;
import org.xmodel.compress.CompressorException;
import org.xmodel.compress.ISerializer;

/**
 * Convenient base implementation of ISerializer.
 */
public abstract class AbstractSerializer implements ISerializer
{
  /* (non-Javadoc)
   * @see org.xmodel.compress.ISerializer#writeValue(java.io.DataOutput, org.xmodel.IModelObject)
   */
  @Override
  public int writeValue( DataOutput output, IModelObject element) throws IOException, CompressorException
  {
    return writeObject( output, element.getValue());
  }
}
