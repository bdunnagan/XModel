package org.xmodel.lss;

import java.io.IOException;

/**
 * Interface for random access storage.
 */
public interface IRandomAccessStore
{
  /**
   * Read bytes at the current position.
   * @param bytes The array where the bytes will be stored.
   * @param offset The offset into the array.
   * @param length The number of bytes to read.
   */
  public void read( byte[] bytes, int offset, int length) throws IOException;
  
  /**
   * Write bytes at the current position.
   * @param bytes The array containing the bytes to write.
   * @param offset The offset into the array.
   * @param length The number of bytes to write.
   */
  public void write( byte[] bytes, int offset, int length) throws IOException;

  /**
   * Read a byte from the current position in the store.
   * @return Returns the byte.
   */
  public byte readByte() throws IOException;
  
  /**
   * Write a byte into the store at the current position.
   * @param b The byte.
   */
  public void writeByte( byte b) throws IOException;
  
  /**
   * Read an integer from the current position in the store.
   * @return Returns the integer.
   */
  public int readInt() throws IOException;
  
  /**
   * Write an integer into the store at the current position.
   * @param value The value.
   */
  public void writeInt( int value) throws IOException;
  
  /**
   * Read an long from the current position in the store.
   * @return Returns the long.
   */
  public long readLong() throws IOException;
  
  /**
   * Write a long into the store at the current position.
   * @param value The value.
   */
  public void writeLong( long value) throws IOException;
  
  /**
   * Mark the specified region as garbage for bookkeeping purposes.  This method, in conjunction
   * with the <code>utility()</code> method, allows clients to track the amount of space in the
   * store that is being utilized.
   * @param position The start of the region.
   * @param length The length of the region.
   */
  public void garbage( long position, long length) throws IOException;
  
  /**
   * @return Returns the percentage utilization of the store.
   */
  public double utility() throws IOException;
  
  /**
   * Flush pending data to the storage device.
   */
  public void flush() throws IOException;
  
  /**
   * Set the position for read and write operations.
   * @param position The position.
   */
  public void seek( long position) throws IOException;
    
  /**
   * @return Returns the current position.
   */
  public long position() throws IOException;

  /**
   * @return Returns the length of the store.
   */
  public long length() throws IOException;
}
