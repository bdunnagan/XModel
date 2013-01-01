package org.xmodel.lss.store;

import java.io.IOException;

public abstract class AbstractStore implements IRandomAccessStore
{
  public AbstractStore()
  {
    buffer = new byte[ 8];
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
      (buffer[ 7] & 0xFF) + 
      ((buffer[ 6] & 0xFF) << 8) + 
      ((buffer[ 5] & 0xFF) << 16) + 
      ((buffer[ 4] & 0xFF) << 24) +
      ((buffer[ 3] & 0xFF) << 32) +
      ((buffer[ 2] & 0xFF) << 40) +
      ((buffer[ 1] & 0xFF) << 48) +
      ((buffer[ 0] & 0xFF) << 56);
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRandomAccessStore#writeLong(long)
   */
  @Override
  public void writeLong( long value) throws IOException
  {
    buffer[ 7] = (byte)(value & 0xFF);
    buffer[ 6] = (byte)((value >> 8) & 0xFF);
    buffer[ 5] = (byte)((value >> 16) & 0xFF);
    buffer[ 4] = (byte)((value >> 24) & 0xFF);
    buffer[ 3] = (byte)((value >> 32) & 0xFF);
    buffer[ 2] = (byte)((value >> 40) & 0xFF);
    buffer[ 1] = (byte)((value >> 48) & 0xFF);
    buffer[ 0] = (byte)((value >> 56) & 0xFF);
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
   * @see org.xmodel.lss.IRandomAccessStore#utility()
   */
  @Override
  public double utility() throws IOException
  {
    long length = length();
    return (length - garbage) / length;
  }

  private byte[] buffer;
  private long garbage;
}
