package org.xmodel.lss;

import java.io.IOException;

/**
 * An interface for extracting keys from database records.
 */
public interface IKeyFormat<K>
{
  /**
   * Read a key from the store.
   * @param store The store.
   * @return Returns the key.
   */
  public K readKey( IRandomAccessStore store) throws IOException;
  
  /**
   * Write a key to the store.
   * @param store The store.
   * @param key The key.
   */
  public void writeKey( IRandomAccessStore store, K key) throws IOException;
  
  /**
   * Extract the key from the database record at the current position in the specified store.
   * @param store The store positioned at the beginning of the record.
   * @return Returns the key.
   */
  public K extractKeyFromRecord( IRandomAccessStore store) throws IOException;
  
  /**
   * Extract the key from the specified database record.
   * @param record The record content.
   * @return Returns the key.
   */
  public K extractKeyFromRecord( byte[] content) throws IOException;
}
