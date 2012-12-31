package org.xmodel.lss;

import java.io.IOException;

public class GarbageCollector<K>
{
  public GarbageCollector( Database<K> db, IRecordFormat<K> format)
  {
    this.db = db;
    this.format = format;
  }
  
  /**
   * Compact a region of the database. The region must begin on a record boundary.
   * @param offset The offset of the region to compact.
   * @param length The length of the region to compact.
   */
  public void compact( long offset, long length) throws IOException
  {
    Record record = new Record();
    IRandomAccessStore store = db.getStore();
    long end = offset + length;
    while( offset < end)
    {
      store.seek( offset);
      format.readRecord( store, record);
      offset = store.position();
      if ( !record.isGarbage()) 
      {
        K key = format.extractKey( record.getContent());
        db.insert( key, record.getContent());
      }
    }
  }
  
  private Database<K> db;
  private IRecordFormat<K> format;
}
