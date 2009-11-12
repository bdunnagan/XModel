/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * Radix.java
 * 
 * Copyright 2009 Robert Arvin Dunnagan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.xmodel.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;

/**
 * A utility class for creating high-radix number representations.
 */
public class Radix
{
  /**
   * Returns the maximum radix supported.
   * @return Returns the maximum radix supported.
   */
  public static int getMaxRadix()
  {
    return radixChars.length;
  }
  
  /**
   * Convert the specified long value to a string with the maximum radix.
   * @param value The value to be converted.
   * @param radix The radix.
   * @return Returns a string representation in the maximum radix.
   */
  public static String convert( long value)
  {
    return convert( value, radixChars.length);
  }
  
  /**
   * Convert the specified long value to a string with the specified radix.
   * @param value The value to be converted.
   * @param radix The radix.
   * @return Returns a string representation in the specified radix.
   */
  public static String convert( long value, int radix)
  {
    StringBuilder sb = new StringBuilder();
    while( value > 0)
    {
      int remainder = (int)(value % radix);
      sb.append( radixChars[ remainder]);
      value /= radix;
    }
    sb.reverse();
    return sb.toString();
  }
  
  /**
   * Convert the specified giant value to a string with the maximum radix.
   * @param value The value to be converted.
   * @param radix The radix.
   * @return Returns a string representation in the maximum radix.
   */
  public static String convert( BigInteger value)
  {
    return convert( value, radixChars.length);
  }
  
  /**
   * Convert the specified giant value to a string with the specified radix.
   * @param value The value to be converted.
   * @param radix The radix.
   * @return Returns a string representation in the specified radix.
   */
  public static String convert( BigInteger value, int radix)
  {
    BigInteger bigRadix = BigInteger.valueOf( radix);
    StringBuilder sb = new StringBuilder();
    while( value.compareTo( BigInteger.ZERO) > 0)
    {
      BigInteger[] result = value.divideAndRemainder( bigRadix);
      sb.append( radixChars[ result[ 1].intValue()]);
      value = result[ 0];
    }
    sb.reverse();
    return sb.toString();
  }
  
  /**
   * Convert the specified string representation of a value using the specified radix.
   * @param value The string representation of a value in the given radix.
   * @param radix The radix of the representation.
   * @return Returns a long.
   */
  public static long convertLong( String value, int radix)
  {
    int length = value.length();
    long result = 0;
    for( int i=0; i<length; i++)
    {
      char c = value.charAt( i);
      if ( c == ' ') continue;
      
      if ( i > 0) result *= radix;
      
      for( int j=0; j<radixChars.length; j++)
        if ( radixChars[ j] == c)
        {
          result += j;
          break;
        }
    }
    
    return result;
  }
  
  /**
   * Convert the specified string representation of a value using the specified radix.
   * @param value The string representation of a value in the given radix.
   * @param radix The radix of the representation.
   * @return Returns a BigInteger.
   */
  public static BigInteger convertBig( String value, int radix)
  {
    int length = value.length();
    BigInteger result = BigInteger.ZERO;
    BigInteger bigRadix = BigInteger.valueOf( radix);
    for( int i=0; i<length; i++)
    {
      char c = value.charAt( i);
      if ( c == ' ') continue;
      
      if ( i > 0) result = result.multiply( bigRadix);
      
      for( int j=0; j<radixChars.length; j++)
        if ( radixChars[ j] == c)
        {
          result = result.add( BigInteger.valueOf( j));
          break;
        }
    }
    
    return result;
  }
  /**
   * Create a large number of radix characters.
   * @return Returns an array of radix characters.
   */
  private static char[] createRadixChars()
  {
    StringBuilder sb = new StringBuilder();
    sb.append( "0123456789");
    sb.append( "abcdefghjkmnopqrstuvwxyz");
    sb.append( "ABCDEFGHJKLMNPQRSTUVWXYZ");
    sb.append( "!@#$%^&*-+=:.<>?~|_");
    return sb.toString().toCharArray();
  }

  private final static char[] radixChars = createRadixChars();
  
  public static void main( String[] args) throws Exception
  {    
    BufferedReader in = new BufferedReader( new InputStreamReader( System.in));
    while( true)
    {
      System.out.print( "> ");
      
      String line = in.readLine();
      String[] parts = line.split( ",");
      if ( parts.length > 1)
      {
        BigInteger value = new BigInteger( parts[ 0].trim());
        int radix = Integer.parseInt( parts[ 1].trim());
        
        String s = Radix.convert( value, radix);
        System.out.printf( "%s -> %d\n\n", s, Radix.convertBig( s, radix));
      }
      else
      {
        BigInteger value = new BigInteger( parts[ 0].trim());
        
        String s = Radix.convert( value, Radix.getMaxRadix());
        System.out.printf( "%s -> %d\n\n", s, Radix.convertBig( s, Radix.getMaxRadix()));
      }
    }
  }
}
