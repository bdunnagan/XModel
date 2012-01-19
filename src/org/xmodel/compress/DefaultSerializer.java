package org.xmodel.compress;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An implementation of ISerializer that provides a mechanism for registering delegates per class.
 */
public class DefaultSerializer implements ISerializer
{
  public DefaultSerializer()
  {
    list = new ArrayList<ISerializer>();
    map = new HashMap<Class<?>, Integer>();
  }
  
  /**
   * Register a delegate implementation of ISerializer.
   * @param clazz The class of object.
   * @param serializer The serializer.
   */
  public void register( Class<?> clazz, ISerializer serializer)
  {
    map.put( clazz, list.size());
    list.add( serializer);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.compress.ISerializer#readObject(java.io.DataInput)
   */
  @Override
  public Object readObject( DataInput input) throws IOException, ClassNotFoundException, CompressorException
  {
    int classID = input.readByte();
    if ( classID < 0) classID = 127 - classID;
    
    if ( classID >= list.size()) 
    {
      throw new ClassNotFoundException( 
        String.format( "Class identifier out of range, %d.", classID));
    }
    
    ISerializer serializer = list.get( classID);
    return serializer.readObject( input);
  }

  /* (non-Javadoc)
   * @see org.xmodel.compress.ISerializer#writeObject(java.io.DataOutput, java.lang.Object)
   */
  @Override
  public int writeObject( DataOutput output, Object object) throws IOException, CompressorException
  {
    Integer classID = map.get( object.getClass());
    if ( classID == null)
    {
      throw new CompressorException( String.format(
        "Class not supported, %s.", object.getClass().getName()));
    }
    
    ISerializer serializer = list.get( classID);
    return serializer.writeObject( output, object);
  }
  
  private List<ISerializer> list;
  private Map<Class<?>, Integer> map;
}
