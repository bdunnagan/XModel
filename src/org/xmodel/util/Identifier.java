package org.xmodel.util;

import java.util.Random;

/**
 * A class that generates identifiers for IModelObject instances. These identifiers
 * are generated randomly with uniform length so that they are more human readable. 
 */
public class Identifier
{
  /**
   * Generate an identifier of the specified length and optional prefix.
   * @param Random The random number generator.
   * @param length The number of characters.
   * @return Returns the identifier.
   */
  public static String generate( Random random, int length)
  {
    StringBuilder sb = new StringBuilder();
    for( int i=0; i<length; i=sb.length())
    {
      long value = random.nextLong();
      sb.append( Radix.convert( value, 36).toUpperCase());
    }
    sb.setLength( length);
    return sb.toString();
  }
}
