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
}
