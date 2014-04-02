package org.xmodel.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.xmodel.log.SLog;

public class HexDump
{
  public static String toString( byte[] bytes)
  {
    return toString( bytes, 0, bytes.length);
  }
  
  public static String toString( byte[] bytes, int offset, int length)
  {
    return toString( ChannelBuffers.wrappedBuffer( bytes, offset, length));
  }
  
  public static String toString( ChannelBuffer buffer)
  {
    StringBuilder sb = new StringBuilder();
    StringBuilder text = new StringBuilder();
    int bpl = 16, n = 0;
    for( int i=0; i<buffer.readableBytes(); i++)
    {
      int c = buffer.getByte( buffer.readerIndex() + i) & 0xFF;
      text.append( (c > 32 && c < 128)? (char)c: '.');
      if ( (n % 4) == 3) text.append( "  ");
      
      if ( n == 0)
      {
        for( int j=0; j<bpl && (i + j) < buffer.readableBytes(); j+=4)
          sb.append( String.format( "|%-8d", i + j));
        sb.append( '\n');
      }
      
      if ( (n % 4) == 0) sb.append( "|");
      sb.append( String.format( "%02x", c));
        
      if ( ++n == bpl) 
      { 
        sb.append( "  ");
        sb.append( text);
        text.setLength( 0);
        sb.append( '\n');
        n=0;
      }
    }
    
    while( n++ < bpl) sb.append( ((n % 4) == 0)? "   ": "  "); 
    sb.append( "  ");
    sb.append( text);
    
    return sb.toString();
  }

  public static void toFile( String file, ChannelBuffer buffer)
  {
    try
    {
      FileOutputStream out = new FileOutputStream( file);
      byte[] bytes = new byte[ buffer.readableBytes()];
      buffer.readBytes( bytes);
      out.write( bytes);
      out.close();
    }
    catch( IOException e)
    {
      SLog.exception( HexDump.class, e);
    }
  }
  
  public static ChannelBuffer load( String file) throws IOException
  {
    ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
    BufferedInputStream stream = new BufferedInputStream( new FileInputStream( file));
    byte[] bytes = new byte[ 4096];
    while( true)
    {
      int nread = stream.read( bytes);
      if ( nread < 0) break;
      buffer.writeBytes( bytes, 0, nread);
    }
    stream.close();
    return buffer;
  }
  
  public static void main( String[] args) throws Exception
  {
    System.out.println( "Enter one or two files separated by comma:");
    System.out.print( "> ");
    
    BufferedReader in  = new BufferedReader( new InputStreamReader( System.in));
    String files = in.readLine();

    int iComma = files.indexOf( ",");
    if ( iComma == -1)
    {
      ChannelBuffer file = load( files);
      System.out.println( toString( file));
    }
    else
    {
      String file1 = files.substring( 0, iComma).trim();
      String file2 = files.substring( iComma+1).trim();
      
      
      
    }
  }
}
