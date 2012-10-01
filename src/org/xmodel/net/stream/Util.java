package org.xmodel.net.stream;

import org.jboss.netty.buffer.ChannelBuffer;

public class Util
{
  /**
   * Dump the content of the specified buffer.
   * @param buffer The buffer.
   * @param indent The indentation before each line.
   * @return Returns a string containing the dump.
   */
  public final static String dump( ChannelBuffer buffer, String indent)
  {
    buffer.markReaderIndex();
    
    StringBuilder sb = new StringBuilder();
    sb.append( indent);
    
    int bpl = 64;
    int count = buffer.readableBytes();
    for( int i=0, n=0; i<count; i++)
    {
      if ( n == 0)
      {
        for( int j=0; j<bpl && (i + j) < count; j+=4)
          sb.append( String.format( "|%-8d", i + j));
        sb.append( String.format( "\n%s", indent));
      }
      
      if ( (n % 4) == 0) sb.append( "|");
      sb.append( String.format( "%02x", buffer.readByte()));
        
      if ( ++n == bpl) 
      { 
        sb.append( String.format( "\n%s", indent));
        n=0;
      }
    }
    
    buffer.resetReaderIndex();
    return sb.toString();
  }
}
