package org.xmodel.util;

import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class generates 64-bit identifiers that is roughly similar to a time-based UUID.  42 bits
 * of the identifier are the low 42 bits of the timestamp returned by System.currentTimeMillis(). 
 * To prevent overlap between distributed systems, this class can be seeded with a 3-bit number 
 * that uniquely identifies the distributed node.  The remaining 17 bits are generated sequentially
 * using a counter.
 * <p>
 * 0 1      3                                         41               64
 * +-+------+-----------------------------------------+-----------------+
 * |0| Node |                Timestamp                |      Counter    |
 * +-+------+-----------------------------------------+-----------------+
 * <p>
 * Note that the timestamp and the counter are in the low order bits to support key clustering.
 * <p>
 * This algorithm guarantees that no two identical values will be generated regardless of the 
 * frequency at which the values are generated.  Values are unique across runtime executions of 
 * the application - in other words, globally unique.
 * <p>
 * The 3-bit node number supports 8 nodes.  However, the identifier cannot be safely converted 
 * to a double-precision (double) if a node value of 7 is used.
 */
public class Identifier64
{
  /**
   * Generate the next identifier.
   * @param node The 3-bit node number.
   * @return Returns the identifier.
   */
  public static long generate( int node)
  {
    long rcount = counter.incrementAndGet();
    long count = rcount & 0x1FFFF;
    if ( count == maxCount) try { Thread.sleep( 1);} catch( Exception e) {}
    
    long time = System.currentTimeMillis() - 1389925031061L;
    int mark = (int)(time & 0x7FFF);
    if ( ((rcount >> 17) & 0x7FFF) != mark)
    {
      count = 0;
      counter.set( mark << 17);
    }
    return (time << 17) & 0x0FFFFFFFFFFFFFFFL | ((long)node << 60) | count;
  }
    
  /**
   * Fast base-32 conversion using easy to read subset of characters.
   * @param v The value.
   * @return Returns the string representation.
   */
  public static String toString( long v)
  {
    StringBuilder sb = new StringBuilder();
    sb.insert( 0, array[ (int)(v & 0x1F)]); v >>= 5;
    sb.insert( 0, array[ (int)(v & 0x1F)]); v >>= 5;
    sb.insert( 0, array[ (int)(v & 0x1F)]); v >>= 5;
    sb.insert( 0, array[ (int)(v & 0x1F)]); v >>= 5;
    sb.insert( 0, array[ (int)(v & 0x1F)]); v >>= 5;
    sb.insert( 0, array[ (int)(v & 0x1F)]); v >>= 5;
    sb.insert( 0, array[ (int)(v & 0x1F)]); v >>= 5;
    sb.insert( 0, array[ (int)(v & 0x1F)]); v >>= 5;
    sb.insert( 0, array[ (int)(v & 0x1F)]); v >>= 5;
    sb.insert( 0, array[ (int)(v & 0x1F)]); v >>= 5;
    sb.insert( 0, array[ (int)(v & 0x1F)]); v >>= 5;
    sb.insert( 0, array[ (int)(v & 0x1F)]); v >>= 5;
    sb.insert( 0, array[ (int)(v & 0x0F)]);
    return sb.toString();
  }
  
  /**
   * Fast base-32 conversion using easy to read subset of characters.
   * @param v The value.
   * @return Returns the value.
   */
  public static long toLong( CharSequence s)
  {
    long v = 0;
    for( int i=0; i<s.length(); i++)
    {
      v <<= 5;
      int c = (int)s.charAt( i);
      if      ( c < 65) v += (c - 48);
      else if ( c < 74) v += (c - 55);
      else if ( c < 82) v += (c - 56);
      else              v += (c - 59);
    }
    return v;
  }
 
  private static char array[] = {
    '0', '1', '2', '3', '4', '5', '6', '7', 
    '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 
    'G', 'H', 'J', 'K', 'L', 'M', 'N', 'R', 
    'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
  };
  
  private static int maxCount = 1 << 17 - 1;
  private static AtomicInteger counter = new AtomicInteger( 0);
  
  public static void main( String[] args) throws Exception
  {
    long x = 1L<<52;
    System.out.printf( "%15.15f\t%d\n", (double)x, x);
    
    long[] b = new long[ 10];
    for( int i=0; i<b.length; i++)
      b[ i] = Identifier64.generate( 7);
    for( int i=0; i<b.length; i++)
      System.out.printf( "%d %s\n", b[ i], toString( b[ i]));
    System.exit( 1);
    
    for( int j=0; j<100; j++)
    {
      long[] a = new long[ 5000000];
      for( int i=0; i<a.length; i++)
        a[ i] = Identifier64.generate( 7);
      
      HashSet<Long> set = new HashSet<Long>();
      for( int i=0; i<a.length; i++)
        set.add( a[ i]);
      
      System.out.printf( "%d = %d\n", a.length, set.size());
    }
  }
}
