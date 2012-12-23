package org.xmodel.lss;

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
  public void read( byte[] bytes, int offset, int length);
  
  /**
   * Write bytes at the current position.
   * @param bytes The array containing the bytes to write.
   * @param offset The offset into the array.
   * @param length The number of bytes to write.
   */
  public void write( byte[] bytes, int offset, int length);

  /**
   * Read an integer from the current position in the store.
   * @return Returns the integer.
   */
  public int readInt();
  
  /**
   * Write an integer into the store at the current position.
   * @param value The value.
   */
  public void writeInt( int value);
  
  /**
   * Read an long from the current position in the store.
   * @return Returns the long.
   */
  public long readLong();
  
  /**
   * Write a long into the store at the current position.
   * @param value The value.
   */
  public void writeLong( long value);
  
  /**
   * Set the position for read and write operations.
   * @param position The position.
   */
  public void seek( long position);
  
  /**
   * @return Returns the current position.
   */
  public long position();
  
  /**
   * Seek to the end.
   */
  public void seekEnd();
}
