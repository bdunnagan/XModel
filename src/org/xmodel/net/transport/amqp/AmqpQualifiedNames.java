package org.xmodel.net.transport.amqp;

import java.util.Random;
import org.xmodel.util.Identifier;

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
    return name + "_" + unique;
  }

  /**
   * Parse the registration name from a qualified registration name.
   * @param name The qualified registration name.
   * @return Returns the unqualified registration name.
   */
  public static String parseRegistrationName( String name)
  {
    int index = name.indexOf( '_');
    if ( index == -1) throw new IllegalStateException( "Registration name is not qualified!");
    return name.substring( 0, index);
  }
  
  private static String unique = Identifier.generate( new Random(), 8);
}
