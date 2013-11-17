package org.xmodel.compress;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MultiByteArrayInputStream extends InputStream
{
  public MultiByteArrayInputStream( List<byte[]> buffers)
  {
    segments = buffers;
  }
  
  /* (non-Javadoc)
   * @see java.io.InputStream#read(byte[], int, int)
   */
  @Override
  public int read( byte[] b, int off, int len) throws IOException
  {
    if ( segmentIndex == segments.size()) return -1;
    
    byte[] buffer = segments.get( segmentIndex);
    int space = buffer.length - bufferIndex;
    if ( space == 0)
    {
      if ( ++segmentIndex == segments.size()) return -1;
      buffer = segments.get( segmentIndex);
      bufferIndex = 0;
      space = buffer.length;
    }
    
    int start = off;
    while( len > space)
    {
      System.arraycopy( buffer, bufferIndex, b, off, space);
      off += space;
      len -= space;
      
      if ( ++segmentIndex == segments.size()) return off - start;
      
      buffer = segments.get( segmentIndex);
      bufferIndex = 0;
      space = buffer.length;
    }
    
    if ( len > 0) 
    {
      System.arraycopy( buffer, bufferIndex, b, off, len);
      off += len;
      bufferIndex += len;
    }
    
    return off - start;
  }

  /* (non-Javadoc)
   * @see java.io.InputStream#read(byte[])
   */
  @Override
  public int read( byte[] b) throws IOException
  {
    return read( b, 0, b.length);
  }

  /* (non-Javadoc)
   * @see java.io.InputStream#read()
   */
  @Override
  public int read() throws IOException
  {
    if ( segmentIndex == segments.size()) return -1;
    
    byte[] buffer = segments.get( segmentIndex);
    int space = buffer.length - bufferIndex;
    if ( space == 0)
    {
      if ( ++segmentIndex == segments.size()) return -1;
      buffer = segments.get( segmentIndex);
      bufferIndex = 0;
    }
    
    return (int)buffer[ bufferIndex++] & 0xFF;
  }

  protected List<byte[]> segments;
  protected int segmentIndex;
  protected int bufferIndex;
  
  public static void main( String[] args) throws Exception
  {
    byte[] bytes = new byte[ 373 * 10000];
    for( int i=0; i<bytes.length; i++)
      bytes[ i] = (byte)(i % 128);
    
    MultiByteArrayOutputStream out = new MultiByteArrayOutputStream();
    for( int i=0; i<bytes.length; i+=373)
      out.write( bytes, i, 373);
    
    List<byte[]> buffers = out.getBuffers();
    System.out.printf( "Count: %d, Max: %d\n", buffers.size(), buffers.get( buffers.size() - 1).length);
    
    bytes = new byte[ 10];
    MultiByteArrayInputStream in = new MultiByteArrayInputStream( buffers);
    
    int n = 0;
    while( true)
    {
      int read = in.read( bytes);
      if ( read == -1) break;
      
      for( int i=0; i<read; i++)
        if ( bytes[ i] != (byte)(n++ % 128))
          System.err.printf( "Fail: %d\n", i);
    }
    
    if ( in.read() != -1)
      System.err.printf( "Fail: expected eof\n");
  }
}
