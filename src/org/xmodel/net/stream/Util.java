package org.xmodel.net.stream;

import java.nio.ByteBuffer;

public class Util
{
  /**
   * Dump the content of the specified buffer.
   * @param buffer The buffer.
   * @return Returns a string containing the dump.
   */
  public final static String dump( ByteBuffer buffer)
  {
    StringBuilder sb = new StringBuilder();
    sb.append( String.format( "[%d, %d] ", buffer.position(), buffer.limit()));
    
    int n=0;
    for( int i=buffer.position(); i<buffer.limit(); i++, n++)
    {
      if ( n == 8) sb.append( " ");
      if ( n == 16) sb.append( "  ");
      if ( n == 32) 
      { 
        sb.append( String.format( "\n[%d, %d] ", i, buffer.limit()));
        n=0;
      }
      
      sb.append( String.format( "%02X", buffer.get( i)));
      
      if ( (i - buffer.position()) > 128)
      {
        sb.append( "...");
        break;
      }
    }
    
    sb.append( "\n");
    return sb.toString();
  }
}
