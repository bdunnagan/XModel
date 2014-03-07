package org.xmodel.net.transport.amqp;

import java.util.Random;

/**
 * When an AMQP client registers with a server, it must provide a unique identifier in addition
 * to the name that it will be known by on the server (registered name).  This class generates 
 * a unique random identifier for the duration of the process.
 */
public class AmqpQualifiedNames
{
  /**
   * Create a registration name that is qualified with the unique identifier.
   * @param name The name.
   * @return Returns the qualified name.
   */
  public static String createQualifiedName( String name)
  {
    return unique+","+name;
  }

  /**
   * Parse the registration name from a qualified registration name.
   * @param name The qualified registration name.
   * @return Returns the unqualified registration name.
   */
  public static String parseRegistrationName( String name)
  {
    int index = name.indexOf( ',');
    if ( index == -1) throw new IllegalStateException( "Registration name is not qualified!");
    return name.substring( index+1);
  }
  
  private static String unique;
  
  static
  {
    if ( unique == null) 
    {
      Random random = new Random();
      long value = random.nextLong();
      if ( value < 0) value = -value;
      unique = Long.toString( value, 36).toUpperCase();
    }
  }
}
