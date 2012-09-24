package org.xmodel.net.stream;

import java.nio.ByteBuffer;

public class Util
{
  /**
   * Dump the content of the specified buffer.
   * @param buffer The buffer.
   * @param indent The indentation before each line.
   * @return Returns a string containing the dump.
   */
  public final static String dump( ByteBuffer buffer, String indent)
  {
    StringBuilder sb = new StringBuilder();
    sb.append( indent);
    
    int bpl = 64;
    for( int i=buffer.position(), n=0; i<buffer.limit(); i++)
    {
      if ( n == 0)
      {
        for( int j=0; j<bpl; j+=4)
          sb.append( String.format( "|%-8d", i + j));
        sb.append( "\n");
      }
      
      if ( (n % 4) == 0) sb.append( "|");
      sb.append( String.format( "%02x", buffer.get( i)));
        
      if ( ++n == bpl) 
      { 
        sb.append( String.format( "\n%s", indent));
        n=0;
      }
    }
    
    return sb.toString();
  }
}
