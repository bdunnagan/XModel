package org.xmodel.compress;

import java.io.IOException;
import org.jboss.netty.buffer.ChannelBuffer;
import org.xmodel.IModelObject;

/**
 * An interface for serialization of objects stored in attributes.  This interface is intended for use
 * by implementations of ICommpressor when attribute values that are not CharSequence instances are
 * encountered.  This interface is not provided in preference to standard Java serialization.
 * Note that only one of each type of method should return a non-null value.
 */
public interface ISerializer
{
  /**
   * Read an object from the specified input.
   * @param input The input.
   * @return Returns the object (may be null).
   */
  public Object readObject( ChannelBuffer input) throws IOException, ClassNotFoundException;
  
  /**
   * Write an object to the specified output.
   * @param output The output.
   * @param object The object.
   * @return Returns the number of bytes written.
   */
  public int writeObject( ChannelBuffer output, IModelObject object) throws IOException;
}
