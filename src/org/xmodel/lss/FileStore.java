package org.xmodel.lss;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileStore<K> extends AbstractStore<K>
{
  public FileStore( String filename) throws IOException
  {
    file = new RandomAccessFile( filename, "rw");
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.lss.IRandomAccessStore#read(byte[], int, int)
   */
  @Override
  public void read( byte[] bytes, int offset, int length) throws IOException
  {
    while( length > 0)
    {
      int nread = file.read( bytes, offset, length);
      if ( nread < 0) throw new EOFException();
      length -= nread;
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRandomAccessStore#write(byte[], int, int)
   */
  @Override
  public void write( byte[] bytes, int offset, int length) throws IOException
  {
    file.write( bytes, offset, length);
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRandomAccessStore#readByte()
   */
  @Override
  public byte readByte() throws IOException
  {
    return (byte)file.read();
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRandomAccessStore#writeByte(byte)
   */
  @Override
  public void writeByte( byte b) throws IOException
  {
    file.write( b);
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
    file.seek( position);
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRandomAccessStore#position()
   */
  @Override
  public long position() throws IOException
  {
    return file.getFilePointer();
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRandomAccessStore#length()
   */
  @Override
  public long length() throws IOException
  {
    return file.length();
  }
  
  private RandomAccessFile file;
}
