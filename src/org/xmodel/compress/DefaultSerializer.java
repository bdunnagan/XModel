package org.xmodel.compress;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jboss.netty.buffer.ChannelBuffer;
import org.xmodel.compress.serial.BooleanSerializer;
import org.xmodel.compress.serial.NumberSerializer;
import org.xmodel.compress.serial.StringSerializer;

/**
 * An implementation of ISerializer that provides a mechanism for registering delegates per class.
 */
public class DefaultSerializer implements ISerializer
{
  public DefaultSerializer()
  {
    classes = new ArrayList<Class<?>>();
    serializers = new ArrayList<ISerializer>();

    register( Object.class, new StringSerializer());
    register( Boolean.class, new BooleanSerializer());
    register( Number.class, new NumberSerializer());
    //register( File.class, new FileSerializer());
  }
  
  /**
   * Register a delegate implementation of ISerializer.
   * @param clazz The class of object.
   * @param serializer The serializer.
   */
  public void register( Class<?> clazz, ISerializer serializer)
  {
    if ( classes.size() == 256) throw new IndexOutOfBoundsException();
    classes.add( clazz);
    serializers.add( serializer);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.compress.ISerializer#readObject(java.io.DataInput)
   */
  @Override
  public Object readObject( ChannelBuffer input) throws IOException, ClassNotFoundException
  {
    int classID = input.readUnsignedByte();
    if ( classID >= serializers.size()) 
    {
      throw new ClassNotFoundException( 
        String.format( "Class identifier out of range, %d.", classID));
    }
    
    ISerializer serializer = serializers.get( classID);
    return serializer.readObject( input);
  }

  /* (non-Javadoc)
   * @see org.xmodel.compress.ISerializer#writeObject(java.io.DataOutput, java.lang.Object)
   */
  @Override
  public int writeObject( ChannelBuffer output, Object object) throws IOException, CompressorException
  {
    int classID = findSerializerClassID( object);   
    if ( classID < 0)
    {
      throw new IOException( String.format(
        "Class not supported, %s.", object.getClass().getName()));
    }
   
    output.writeByte( classID);
    
    ISerializer serializer = serializers.get( classID);
    return 1 + serializer.writeObject( output, object);
  }
  
  /**
   * Searches the registered serializer classes in reverse order for a class to which the specified object can be assigned.
   * @param object The object.
   * @return Returns -1 or the index of the first matching class.
   */
  private final int findSerializerClassID( Object object)
  {
    Class<?> clazz = object.getClass();
    for( int i=classes.size() - 1; i >= 0; i--)
    {
      if ( classes.get( i).isAssignableFrom( clazz))
        return i;
    }
    return -1;
  }

  private List<Class<?>> classes;
  private List<ISerializer> serializers;
}
