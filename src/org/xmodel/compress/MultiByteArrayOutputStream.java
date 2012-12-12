package org.xmodel.compress;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MultiByteArrayOutputStream extends OutputStream
{
  public MultiByteArrayOutputStream()
  {
    this( 32);
  }
  
  public MultiByteArrayOutputStream( int initialCapacity)
  {
    segments = new ArrayList<byte[]>();
    defaultLength = initialCapacity;
  }
  
  /* (non-Javadoc)
   * @see java.io.OutputStream#write(byte[], int, int)
   */
  @Override
  public void write( byte[] b, int off, int len) throws IOException
  {
    if ( buffer == null || bufferIndex == buffer.length) newSegment( defaultLength);
    
    int space = buffer.length - bufferIndex;
    if ( len < space)
    {
      System.arraycopy( b, off, buffer, bufferIndex, len);
      bufferIndex += len;
    }
    else
    {
      System.arraycopy( b, off, buffer, bufferIndex, space);
      int remaining = len - space;
      newSegment( remaining);
      System.arraycopy( b, off + space, buffer, bufferIndex, remaining);
      bufferIndex += remaining;
    }
  }

  /* (non-Javadoc)
   * @see java.io.OutputStream#write(byte[])
   */
  @Override
  public void write( byte[] b) throws IOException
  {
    write( b, 0, b.length);
  }

  /* (non-Javadoc)
   * @see java.io.OutputStream#write(int)
   */
  @Override
  public void write( int b) throws IOException
  {
    if ( buffer == null || bufferIndex == buffer.length) newSegment( defaultLength);
    buffer[ bufferIndex++] = (byte)b;
  }

  /**
   * Create new segment.
   * @param length The minimum length.
   */
  protected void newSegment( int length)
  {
    if ( buffer != null) segments.add( buffer);
    
    if ( length < defaultLength) length = defaultLength;
    buffer = new byte[ length];
    bufferIndex = 0;
    
    if ( defaultLength < maxDefaultLength)
      defaultLength *= 2;
  }
  
  /**
   * @return Returns the byte arrays containing the output stream data.
   */
  public List<byte[]> getBuffers()
  {
    if ( buffer == null) return Collections.emptyList();
    
    // compact current segment
    if ( bufferIndex < defaultLength)
    {
      byte[] array = new byte[ bufferIndex];
      System.arraycopy( buffer, 0, array, 0, bufferIndex);
      buffer = array;
    }
    
    // add to list
    segments.add( buffer);
    
    // cleanup
    buffer = null;
    bufferIndex = 0;
    
    return segments;
  }
  
  /**
   * Returns the total number of bytes in the specified buffers.
   * @param buffers The buffers.
   * @return Returns the total number of bytes in the specified buffers.
   */
  public static int getLength( List<byte[]> buffers)
  {
    int count = 0;
    for( byte[] buffer: buffers)
      count += buffer.length;
    return count;
  }
  
  protected static int maxDefaultLength = 65536;
  
  protected int defaultLength;
  protected List<byte[]> segments;
  protected byte[] buffer;
  protected int bufferIndex;
}
