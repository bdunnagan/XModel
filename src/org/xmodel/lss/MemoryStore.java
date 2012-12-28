package org.xmodel.lss;

import java.io.IOException;

public class MemoryStore extends AbstractStore
{
  public MemoryStore( int capacity)
  {
    data = new byte[ capacity];
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.lss.IRandomAccessStore#read(byte[], int, int)
   */
  @Override
  public void read( byte[] bytes, int offset, int length) throws IOException
  {
    if ( pos + length > end) throw new IndexOutOfBoundsException();
    System.arraycopy( data, pos, bytes, offset, length);
    pos += length;
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRandomAccessStore#write(byte[], int, int)
   */
  @Override
  public void write( byte[] bytes, int offset, int length) throws IOException
  {
    System.arraycopy( bytes, offset, data, pos, length);
    pos += length;
    if ( pos > end) end = pos;
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRandomAccessStore#readByte()
   */
  @Override
  public byte readByte() throws IOException
  {
    return data[ pos++];
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRandomAccessStore#writeByte(byte)
   */
  @Override
  public void writeByte( byte b) throws IOException
  {
    data[ pos++] = b;
    if ( pos > end) end = pos;
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRandomAccessStore#flush()
   */
  @Override
  public void flush() throws IOException
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRandomAccessStore#seek(long)
   */
  @Override
  public void seek( long position) throws IOException
  {
    pos = (int)position;
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRandomAccessStore#position()
   */
  @Override
  public long position() throws IOException
  {
    return pos;
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRandomAccessStore#length()
   */
  @Override
  public long length() throws IOException
  {
    return end;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    
    int bpl = 32;
    for( int i=0, n=0; i<end; i++)
    {
      if ( n == 0)
      {
        if ( pos >= i && pos < (i + bpl))
        {
          int cpos = 2 * (pos - i) + ((pos - i) / 4) + 1;
          for( int k=0; k<cpos; k++) sb.append( '-');
          sb.append( 'v');
          sb.append( '\n');
        }
        
        for( int j=0; j<bpl && (i + j) < end; j+=4)
          sb.append( String.format( "|%-8X", i + j));
        sb.append( "\n");
      }
      
      if ( (n % 4) == 0) sb.append( "|");
      sb.append( String.format( "%02x", data[ i]));
        
      if ( ++n == bpl) 
      { 
        for( int j=0; j<bpl; j++)
        {
          if ( (j % 4) == 0) sb.append( ' ');
          char c = (char)data[ i + 1 - bpl + j];
          if ( c < 32 || c > 127) c = '.';
          sb.append( c);
        }
        sb.append( "\n");
        n=0;
      }
    }
    
    return sb.toString();
  }

  private byte[] data;
  private int pos;
  private int end;
}
