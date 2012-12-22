package org.xmodel.lss;

import java.util.NavigableMap;

/**
 * An IIndex that implements a log structured storage algorithm in memory for educational purposes.
 */
public class MemoryLSS implements IIndex
{
  public MemoryLSS()
  {
    data = new byte[ 1000];
    position = 8;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.lss.IStore#get(byte[])
   */
  @Override
  public byte[] get( byte[] key)
  {
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IStore#put(byte[], byte[])
   */
  @Override
  public void put( byte[] key, byte[] bytes)
  {
    // write record
    writeLong( bytes.length);
    write( bytes);
    
    // write index
    
    // update index pointer
  }
  
  /**
   * Write a long.
   * @param value The value.
   */
  private void writeLong( long value)
  {
    byte[] bytes = new byte[ 8];
    
    for( int i=7; i>=0; i--)
    {
      bytes[ i] = (byte)(value & 0xFF);
      value >>= 8;
    }
    
    write( bytes);
  }

  /**
   * Read a long at the specified position.
   * @param position The position.
   * @return Returns the value.
   */
  private long readLong( int position)
  {
    byte[] bytes = new byte[ 8];
    read( bytes);
    
    long value = 0;
    for( int i=0; i<8; i++)
    {
      value += bytes[ i];
      value <<= 8;
    }    
    return value;
  }
  
  /**
   * Write bytes.
   * @param bytes The bytes.
   */
  private void write( byte[] bytes)
  {
  }
  
  /**
   * Read bytes.
   */
  private void read( byte[] bytes)
  {
  }
  
  /**
   * Seek to the specified position.
   * @param position The position.
   */
  private void seek( int position)
  {
    this.position = position;
  }
    
  private byte[] data;
  private int position;
  private NavigableMap<byte[], Integer> index;
}
