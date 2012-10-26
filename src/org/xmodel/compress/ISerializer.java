package org.xmodel.compress;

import java.io.IOException;
import org.jboss.netty.buffer.ChannelBuffer;

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
  public Object readObject( ChannelBuffer input) throws IOException, ClassNotFoundException, CompressorException;
  
  /**
   * Write an object to the specified output.
   * @param output The output.
   * @param object The object.
   * @return Returns the number of bytes written.
   */
  public int writeObject( ChannelBuffer output, Object object) throws IOException, CompressorException;
}
