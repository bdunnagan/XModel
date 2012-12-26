package org.xmodel.lss;

/**
 * An interface for extracting keys from database records.
 */
public interface IKeyParser<K>
{
  /**
   * Extract the key from the specified database record.
   * @param record The record.
   * @return Returns the key.
   */
  public K extract( byte[] record);
}
