package org.xmodel.compress;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DeflaterOutputStream;

/**
 * An implementation of OutputStream that is provides methods necessary to minimize data copying.
 */
public final class TabularOutputStream extends OutputStream
{
  public TabularOutputStream()
  {
    stream = new ByteArrayOutputStream();
    writes = new ArrayList<byte[]>();
  }
  
  /**
   * Reset this stream.
   */
  public void reset()
  {
    writes.clear();
    count = 0;
  }

  /**
   * Set whether to post-compress with the DEFLATE algorithm.
   * @param deflate True or false.
   */
  public void setDeflate( boolean deflate)
  {
    this.deflate = deflate;
  }
  
  /**
   * Set whether the table is predefined.
   * @param predefined True or false.
   */
  public void setPredefined( boolean predefined)
  {
    this.predefined = predefined;
  }
  
  /**
   * Set the offset of the tag table.
   * @param offset The offset.
   */
  public void setTableOffset( int offset)
  {
    if ( offset < (1 << 8)) tableOffsetSize = 1;
    else if ( offset < (1 << 16)) tableOffsetSize = 2;
    else if ( offset < (1 << 32)) tableOffsetSize = 4;
    else tableOffsetSize = 8;
    
    byte[] bytes = new byte[ tableOffsetSize];
    for( int i=0; i<tableOffsetSize; i++)
    {
      bytes[ tableOffsetSize - 1 - i] = (byte)(offset & 0xff);
      offset >>= 8;
    }
    writes.add( 0, bytes);
  }
  
  /* (non-Javadoc)
   * @see java.io.OutputStream#write(byte[], int, int)
   */
  @Override
  public void write( byte[] bytes, int offset, int length) throws IOException
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see java.io.OutputStream#write(byte[])
   */
  @Override
  public void write( byte[] bytes) throws IOException
  {
    writes.add( bytes);
    count += bytes.length;
  }

  /* (non-Javadoc)
   * @see java.io.OutputStream#write(int)
   */
  @Override
  public void write( int b) throws IOException
  {
    writes.add( new byte[] { (byte)b});
    count++;
  }
  
  /**
   * @return Returns the number of bytes written not including header or table offset.
   */
  public int getCount()
  {
    return count;
  }

  /**
   * @return Returns the backing byte array.
   */
  public byte[] toByteArray()
  {
    stream.reset();

    try
    {
      int header = (byte)(predefined? 0x80: 0x00);
      header |= (byte)(deflate? 0x40: 0x00);
      
      switch( tableOffsetSize)
      {
        case 1: break;
        case 2: header |= 0x01; break;
        case 4: header |= 0x02; break;
        case 8: header |= 0x03; break;
      }
      
      stream.write( header);
      
      OutputStream out = deflate? new DeflaterOutputStream( stream): stream;
      for( byte[] write: writes) out.write( write);
      out.close();
      
      return stream.toByteArray();
    }
    catch( IOException e)
    {
      throw new IllegalStateException();
    }
  }
  
  private boolean predefined;
  private boolean deflate;
  private int tableOffsetSize;
  private List<byte[]> writes;
  private int count;
  private ByteArrayOutputStream stream;
}
