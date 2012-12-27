package org.xmodel.lss;

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
      (int)buffer[ 3] & 0xFF + 
      (int)(buffer[ 2] << 8) & 0xFF + 
      (int)(buffer[ 1] << 16) & 0xFF + 
      (int)(buffer[ 0] << 24) & 0xFF;
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
      (int)buffer[ 7] & 0xFF + 
      (int)(buffer[ 6] << 8) & 0xFF + 
      (int)(buffer[ 5] << 16) & 0xFF + 
      (int)(buffer[ 4] << 24) & 0xFF +
      (int)(buffer[ 3] << 32) & 0xFF +
      (int)(buffer[ 2] << 40) & 0xFF +
      (int)(buffer[ 1] << 48) & 0xFF +
      (int)(buffer[ 0] << 56) & 0xFF;
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

  private byte[] buffer;
}
