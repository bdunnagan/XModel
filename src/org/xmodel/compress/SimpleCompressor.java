package org.xmodel.compress;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import org.xmodel.IModelObject;
import org.xmodel.net.stream.Util;
import org.xmodel.xml.IXmlIO.Style;
import org.xmodel.xml.XmlIO;

public class SimpleCompressor extends AbstractCompressor
{
  public SimpleCompressor()
  {
    xmlIO = new XmlIO();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.compress.ICompressor#compress(org.xmodel.IModelObject)
   */
  public byte[] compress( IModelObject element) throws CompressorException
  {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    compress( element, stream);
    byte[] bytes = stream.toByteArray();
    return bytes;
  }

  /* (non-Javadoc)
   * @see org.xmodel.compress.ICompressor#decompress(byte[], int)
   */
  public IModelObject decompress( byte[] bytes, int offset) throws CompressorException
  {
    ByteArrayInputStream stream = new ByteArrayInputStream( bytes, offset, bytes.length - offset);
    IModelObject result = decompress( stream);
    return result;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.compress.ICompressor#compress(org.xmodel.IModelObject, java.io.OutputStream)
   */
  @Override
  public void compress( IModelObject element, OutputStream stream) throws CompressorException
  {
    try
    {
      DeflaterOutputStream out = new DeflaterOutputStream( stream);
      xmlIO.write( element, out);
      out.finish();
    }
    catch( Exception e)
    {
      throw new CompressorException( e);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.compress.ICompressor#decompress(java.io.InputStream)
   */
  @Override
  public IModelObject decompress( InputStream stream) throws CompressorException
  {
    try
    {
      InflaterInputStream in = new InflaterInputStream( stream);
      return xmlIO.read( in);
    }
    catch( Exception e)
    {
      throw new CompressorException( e);
    }
  }
  
  public static String toString( byte[] bytes, int bpl)
  {
    StringBuilder sb = new StringBuilder();
    
    int n=0;
    for( int i=0; i<bytes.length; i++)
    {
      if ( n == 0)
      {
        for( int j=0; j<bpl; j+=4) sb.append( String.format( "|%-8d", i + j));
        sb.append( "\n");
      }
      
      if ( (n % 4) == 0) sb.append( "|");
      sb.append( String.format( "%02x", bytes[ i]));
        
      if ( ++n == bpl) 
      { 
        sb.append( " ");
        for( int j=bpl-1; j>=0; j--)
        {
          byte b = bytes[ i-j];
          sb.append( (b >= 32)? (char)b: ".");
          if ( (j % 4) == 0) sb.append( " ");
        }
        
        sb.append( "\n");
        n=0;
      }
    }

    if ( n > 0)
    {
      for( int i=n; i<bpl; i++) 
      {
        if ( (i % 4) == 0) sb.append( "|");
        sb.append( "__");
      }
      
      for( int i=bytes.length - n; i<bytes.length; i++)
      {
        if ( (i % 4) == 0) sb.append( " ");
        byte b = bytes[ i];
        sb.append( (b >= 32)? (char)b: ".");
      }
      sb.append( "\n");
    }
    
    return sb.toString();
  }
  
  private XmlIO xmlIO;
  
  public static void main( String[] args) throws Exception 
  {
    String text =
        "<x id='A'>\n" +
        "  <y>B</y>\n" +
        "</x>";
    
    ICompressor compressor = new TabularCompressor( true, false);
    
    IModelObject element = new XmlIO().read( text);
    for( int i=0; i<5; i++)
    {
      byte[] bytes = compressor.compress( element);
      System.out.printf( "%d -> %d\n", text.length(), bytes.length);
      System.out.println( toString( bytes, 16));
    
      element = compressor.decompress( bytes, 0);
      System.out.println( XmlIO.write( Style.printable, element));
    }
  }
}
