package org.xmodel.lss.store;

import java.io.IOException;

public abstract class AbstractStore implements IRandomAccessStore
{
  public AbstractStore()
  {
    buffer = new byte[ 8];
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.lss.store.IRandomAccessStore#readShort()
   */
  @Override
  public short readShort() throws IOException
  {
    read( buffer, 0, 2);
    return (short)( (buffer[ 1] & 0xFF) + ((buffer[ 0] & 0xFF) << 8));
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.store.IRandomAccessStore#writeShort(short)
   */
  @Override
  public void writeShort( short value) throws IOException
  {
    buffer[ 1] = (byte)(value & 0xFF);
    buffer[ 0] = (byte)((value >> 8) & 0xFF);
    write( buffer, 0, 2);
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRandomAccessStore#readInt()
   */
  @Override
  public int readInt() throws IOException
  {
    read( buffer, 0, 4);
    
    return 
      (buffer[ 3] & 0xFF) +
      ((buffer[ 2] & 0xFF) << 8) +
      ((buffer[ 1] & 0xFF) << 16) +
      ((buffer[ 0] & 0xFF) << 24);
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRandomAccessStore#writeInt(int)
   */
  @Override
  public void writeInt( int value) throws IOException
  {
    buffer[ 3] = (byte)(value & 0xFF);
    buffer[ 2] = (byte)((value >> 8) & 0xFF);
    buffer[ 1] = (byte)((value >> 16) & 0xFF);
    buffer[ 0] = (byte)((value >> 24) & 0xFF);
    write( buffer, 0, 4);
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRandomAccessStore#readLong()
   */
  @Override
  public long readLong() throws IOException
  {
    read( buffer, 0, 8);
    return 
      (buffer[ 7] & 0xFFL) + 
      ((buffer[ 6] & 0xFFL) << 8) + 
      ((buffer[ 5] & 0xFFL) << 16) + 
      ((buffer[ 4] & 0xFFL) << 24) +
      ((buffer[ 3] & 0xFFL) << 32) +
      ((buffer[ 2] & 0xFFL) << 40) +
      ((buffer[ 1] & 0xFFL) << 48) +
      ((buffer[ 0] & 0xFFL) << 56);
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRandomAccessStore#writeLong(long)
   */
  @Override
  public void writeLong( long value) throws IOException
  {
    buffer[ 7] = (byte)(value & 0xFFL);
    buffer[ 6] = (byte)((value >> 8) & 0xFFL);
    buffer[ 5] = (byte)((value >> 16) & 0xFFL);
    buffer[ 4] = (byte)((value >> 24) & 0xFFL);
    buffer[ 3] = (byte)((value >> 32) & 0xFFL);
    buffer[ 2] = (byte)((value >> 40) & 0xFFL);
    buffer[ 1] = (byte)((value >> 48) & 0xFFL);
    buffer[ 0] = (byte)((value >> 56) & 0xFFL);
    write( buffer, 0, 8);
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRandomAccessStore#garbage(long, long)
   */
  @Override
  public void garbage( long position, long length) throws IOException
  {
    garbage += length;
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.store.IRandomAccessStore#garbage()
   */
  @Override
  public long garbage() throws IOException
  {
    return garbage;
  }

  private byte[] buffer;
  private long garbage;
}
