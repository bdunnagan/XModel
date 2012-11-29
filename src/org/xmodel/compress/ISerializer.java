package org.xmodel.compress;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.xmodel.IModelObject;

/**
 * A serialization interface for implementing compact representations of a small domain of data-types (e.g. XPath 1.0).
 */
public interface ISerializer
{
  /**
   * Read an object from the specified input.
   * @param input The input.
   * @return Returns the object (may be null).
   */
  public Object readObject( DataInput input) throws IOException, ClassNotFoundException;
  
  /**
   * Write an object to the specified output.
   * @param output The output.
   * @param object The object.
   * @return Returns the number of bytes written.
   */
  public int writeObject( DataOutput output, Object object) throws IOException, CompressorException;
  
  /**
   * Write the value of the specified element to the specified output.
   * @param output The output.
   * @param element The element.
   * @return Rturns the number of bytes written.
   */
  public int writeValue( DataOutput output, IModelObject element) throws IOException, CompressorException;
}
