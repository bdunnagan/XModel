package org.xmodel.util;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class generates 64-bit identifiers that is roughly similar to a time-based UUID.  43 bits
 * of the identifier are the low 42 bits of the timestamp returned by System.currentTimeMillis(). 
 * To prevent overlap between distributed systems, this class can be seeded with a 3-bit number 
 * that uniquely identifies the distributed node.  The remaining 17 bits are generated sequentially
 * using a counter.
 * <p>
 * 0 1      3                                         47               64
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
    return ((long)node << 60) | counter.incrementAndGet();
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
  
  private static AtomicLong counter = new AtomicLong( ((System.currentTimeMillis() - 1389925031061L) & 0x1FFFFFFFFFL) << 17L);
  
  public static void main( String[] args) throws Exception
  {
    final ConcurrentLinkedQueue<Long> queue = new ConcurrentLinkedQueue<Long>();

    System.out.printf( "%d\n", Identifier64.generate( 1) - Identifier64.generate( 0));
    System.exit( 1);
    
    
    ExecutorService executor = Executors.newFixedThreadPool( 100);
    Runnable runnable = new Runnable() {
      public void run()
      {
        queue.offer( Identifier64.generate( 0));
      }
    };
    
    for( int i=0; i<1000; i++)
    {
      executor.execute( runnable);
    }

    Thread.sleep( 1000);
    Set<Long> set = new HashSet<Long>();
    for( int i=0; i<1000; i++)
    {
      Long id = queue.poll();
      if ( set.contains( id))
        throw new IllegalStateException();
      set.add( id);
      System.out.printf( "%d\n", id);
    }
    
    executor.shutdownNow();
  }
}
