package org.xmodel.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmodel.log.SLog;

public class HexDump
{
  public static byte[] parse( String hexDump)
  {
    Pattern regex = Pattern.compile( "[|]([a-zA-Z0-9]{2,8})");
    Matcher matcher = regex.matcher( hexDump);
    
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    int eol = hexDump.indexOf( '\n'), neol;
    int line = 0;
    while( eol >= 0)
    {
      neol = hexDump.indexOf( '\n', eol + 1);
      
      if ( (line % 2) == 0)
      {
        matcher.region( eol+1, neol);
        while ( matcher.find())
        {
          String hex = matcher.group( 1);
          for( int i=0; i<hex.length(); i+=2)
            out.write( Integer.parseInt( hex.substring( i, i+2), 16));
        }
      }
      
      eol = neol;
      line++;
    }
    
    return out.toByteArray();
  }
  
  public static String toString( byte[] bytes)
  {
    return toString( bytes, 0, bytes.length);
  }
  
  public static String toString( byte[] bytes, int offset, int length)
  {
    return toString( Unpooled.wrappedBuffer( bytes, offset, length));
  }
  
  public static String toString( ByteBuf buffer)
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

  public static void toFile( String file, ByteBuf buffer)
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
  
  public static ByteBuf load( String file) throws IOException
  {
    ByteBuf buffer = Unpooled.buffer();
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
}
