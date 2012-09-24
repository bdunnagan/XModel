package org.xmodel.compress;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.InflaterInputStream;

public final class TabularInputStream extends InputStream
{
  /**
   * Reset the stream.
   */
  public void reset()
  {
    predefined = false;
    stream = null;
    bytes = null;
    start = 0;
    offset = 0;
  }
  
  /**
   * Set the source array.
   * @param bytes The source array.
   * @param offset The offset into the source array.
   */
  public void reset( byte[] bytes, int offset)
  {
    int header = (bytes[ offset] & 0xff);
    predefined = (header & 0x80) != 0;
    
    if ( (header & 0x40) != 0)
    {
      stream = new InflaterInputStream( new ByteArrayInputStream( bytes, offset + 1, bytes.length));
    }
    else
    {
      this.stream = null;
      this.bytes = bytes;
      this.start = offset;
      this.offset = tableOffsetSize + 1;
    }
    
    int tableOffsetMask = (header & 0x0F);
    switch( tableOffsetMask)
    {
      case 0: tableOffsetSize = 1; break;
      case 1: tableOffsetSize = 2; break;
      case 2: tableOffsetSize = 4; break;
      case 3: tableOffsetSize = 8; break;
    }
  }
  
  /**
   * Set the source for the stream.
   * @param stream The source stream.
   */
  public void reset( InputStream stream) throws IOException
  {
    int header = stream.read();
    predefined = (header & 0x80) != 0; 
    
    if ( (header & 0x40) != 0)
    {
      this.stream = new InflaterInputStream( stream);
    }
    else
    {
      this.stream = stream;
    }
  }
  
  /**
   * @return Returns the predefined flag in the header byte.
   */
  public boolean isPredefined()
  {
    return predefined;
  }
  
  /**
   * Point the stream at the table.
   */
  public void pointToTable() throws IOException
  {
    offset = 0;
    
    if ( stream != null)
    {
      for( int i=0; i<tableOffsetSize; i++)
      {
        offset += stream.read();
        offset <<= 8;
      }

      // load skipped bytes
      bytes = new byte[ offset];
      stream.read( bytes);
      start = -tableOffsetSize - 1;
    }
    else
    {
      for( int i=0; i<tableOffsetSize; i++)
      {
        if ( i > 0) offset <<= 8;
        offset += bytes[ start + i + 1];
      }
    }
    
    offset += tableOffsetSize + 1;
  }
  
  /**
   * Point the stream to the body.  Must be called after calling reading table.
   */
  public void pointToBody()
  {
    offset = start + 1 + (predefined? 0: tableOffsetSize);
    stream = null;
  }
  
  /* (non-Javadoc)
   * @see java.io.InputStream#read(byte[], int, int)
   */
  @Override
  public int read( byte[] bytes, int offset, int length) throws IOException
  {
    if ( stream != null) return stream.read( bytes, offset, length);
    
    int rout = length - offset;
    int rin = bytes.length - offset;
    int count = (rin < rout)? rin: rout;
    System.arraycopy( this.bytes, this.offset, bytes, offset, count);
    
    this.offset += count;
    return count;
  }

  /* (non-Javadoc)
   * @see java.io.InputStream#read(byte[])
   */
  @Override
  public int read( byte[] bytes) throws IOException
  {
    return read( bytes, 0, bytes.length);
  }

  /* (non-Javadoc)
   * @see java.io.InputStream#read()
   */
  @Override
  public int read() throws IOException
  {
    if ( stream != null) return stream.read();
    return bytes[ offset++];
  }

  private byte[] bytes;
  private int start;
  private int offset;
  private boolean predefined;
  private int tableOffsetSize;
  private InputStream stream;
}
