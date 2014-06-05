package org.xmodel.util;

public class ByteUtil
{
  public static void toString( byte[] bytes, int offset, int length, int bytesPerLine, StringBuilder sb)
  {
    for( int i=0, n=0; i<length; i++)
    {
      if ( n == 0)
      {
        for( int j=0; j < bytesPerLine && (i + j) < length; j+=4)
          sb.append( String.format( "|%-8d", i + j));
        sb.append( '\n');
      }
      
      if ( (n % 4) == 0) sb.append( "|");
      sb.append( String.format( "%02x", bytes[ offset + i]));
        
      if ( ++n == bytesPerLine) 
      { 
        sb.append( " ");
        for( int j=0; j<bytesPerLine; j++)
          sb.append( String.format( "%c", (char)bytes[ offset + i - bytesPerLine + j]));
        
        sb.append( '\n');
        n=0;
      }
    }
  }
}
