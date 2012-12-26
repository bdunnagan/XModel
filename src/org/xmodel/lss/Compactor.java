package org.xmodel.lss;

public class Compactor<K>
{
  /**
   * Compact a region of the specified database. The region must begin on a record boundary.
   * @param db The database.
   * @param parser The key parser.
   * @param offset The offset of the region to compact.
   * @param length The length of the region to compact.
   */
  public void compact( Database<K> db, IKeyParser<K> parser, long offset, long length)
  {
    IRandomAccessStore<K> store = db.getStore();
    store.seek( offset);
    while( length > 0)
    {
      byte recordHeader = store.readByte();
      long recordLength = store.readLong();
      if ( recordHeader == 0) 
      {
        store.seek( offset);
        byte[] record = db.readRecord();
        K key = parser.extract( record);
        db.insert( key, record);
      }
      
      offset += recordLength;
      length -= recordLength;
    }
  }
}
