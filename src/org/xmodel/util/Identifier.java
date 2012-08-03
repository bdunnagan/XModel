package org.xmodel.util;

import java.util.Random;

/**
 * A class that generates identifiers for IModelObject instances. These identifiers
 * are generated randomly with uniform length so that they are more human readable.
 * Identifiers always begin with a letter.
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
    
    for( int i=0; i<length; )
    {
      int r = random.nextInt();
      if ( i == 0 && (r & 0x1f) < 8) r += 8;
      sb.append( array[ (r & 0x1f)]);
      if ( ++i == length) break;
      sb.append( array[ (r >> 5 & 0x1f)]);
      if ( ++i == length) break;
      sb.append( array[ (r >> 10 & 0x1f)]);
      if ( ++i == length) break;
      sb.append( array[ (r >> 15 & 0x1f)]);
      if ( ++i == length) break;
      sb.append( array[ (r >> 21 & 0x1f)]);
      if ( ++i == length) break;
      sb.append( array[ (r >> 27 & 0x1f)]);
      if ( ++i == length) break;
    }
    
    return sb.toString();
  }
  
  private static char array[] = {
    '2', '3', '4', '5', '6', '7', '8', '9', 
    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 
    'J', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 
    'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
  };
  
  public static void main( String[] args) throws Exception
  {
    Random random = new Random();
    for( int i=0; i<200; i++)
    {
      System.out.printf( Identifier.generate( random, 1));
    }
  }
}
