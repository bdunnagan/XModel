package org.xmodel.lss;

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
  public K read( IRandomAccessStore<K> store);
  
  /**
   * Write a key to the store.
   * @param store The store.
   * @param key The key.
   */
  public void write( IRandomAccessStore<K> store, K key);
  
  /**
   * Extract the key from the specified database record.
   * @param record The record.
   * @return Returns the key.
   */
  public K extract( byte[] record);
}
