package org.xmodel.lss;

/**
 * An interface for storing arbitrary records associated with arbitrary keys.
 */
public interface IIndex
{
  /**
   * Returns the record associated with the specified key.
   * @param key The binary key used to index this record.
   * @return Returns the record associated with the specified key.
   */
  public byte[] get( byte[] key);
  
  /**
   * Put the specified record into the store under the specified index key.
   * @param key The binary key used to index this record.
   * @param bytes The bytes of the record.
   */
  public void put( byte[] key, byte[] bytes);
}
