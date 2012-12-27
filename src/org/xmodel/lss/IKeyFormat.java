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
  public K read( IRandomAccessStore store) throws IOException;
  
  /**
   * Write a key to the store.
   * @param store The store.
   * @param key The key.
   */
  public void write( IRandomAccessStore store, K key) throws IOException;
  
  /**
   * Extract the key from the specified database record.
   * @param record The record.
   * @return Returns the key.
   */
  public K extract( byte[] record);
}
