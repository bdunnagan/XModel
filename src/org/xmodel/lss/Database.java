package org.xmodel.lss;

import java.io.IOException;
import java.util.Comparator;

/**
 * Experimental implementation of a log-structured database.
 */
public class Database<K>
{
  public Database( IRandomAccessStore store, IRecordFormat<K> recordFormat) throws IOException
  {
    this( store, recordFormat, null);
  }
  
  public Database( IRandomAccessStore store, IRecordFormat<K> recordFormat, Comparator<K> comparator) throws IOException
  {
    this.store = store;
    this.btree = new BTree<K>( 1000, recordFormat, store, comparator);
    this.recordFormat = recordFormat;
    finishIndex( recordFormat);
  }
  
  public Database( BTree<K> btree, IRandomAccessStore store, IRecordFormat<K> recordFormat) throws IOException
  {
    this.store = store;
    this.btree = btree;
    this.recordFormat = recordFormat;
    finishIndex( recordFormat);
  }
  
  /**
   * Read and index the records that follow the last b+tree root in the database.
   * @param recordFormat The record format.
   */
  protected void finishIndex( IRecordFormat<K> recordFormat) throws IOException
  {
    while( true)
    {
      long pointer = store.position();
      if ( pointer >= store.length()) break;
      
      byte header = store.readByte();
      if ( header == 0)
      {
        K key = recordFormat.extractKeyAndAdvance( store);
        if ( key != null)
        {
          // insert record and restore seek position
          long position = store.position();
          btree.insert( key, pointer);
          store.seek( position);
        }
      }
    }
  }
  
  /**
   * Insert a record into the database.
   * @param key The key.
   * @param record The record.
   */
  public void insert( K key, byte[] record) throws IOException
  {
    store.seek( store.length());
    long position = store.position();
    writeRecord( key, record);
    position = btree.insert( key, position);
    if ( position > 0) markGarbage( position);
  }
  
  /**
   * Delete a record from the database.
   * @param key The key.
   */
  public void delete( K key) throws IOException
  {
    long position = btree.delete( key);
    if ( position > 0) markGarbage( position);
  }
  
  /**
   * Query a record from the database.
   * @param key The key.
   * @return Returns null or the record.
   */
  public byte[] query( K key) throws IOException
  {
    long position = btree.get( key);
    if ( position > 0) 
    {
      store.seek( position);
      Record<K> record = readRecord();
      return record.getContent();
    }
    return null;
  }
  
  /**
   * Write a record.
   * @param content The record content.
   */
  public void writeRecord( K key, byte[] content) throws IOException
  {
    Record<K> record = new Record<K>( recordFormat, key, content, false);
    recordFormat.writeRecord( store, record);
  }
  
  /**
   * Read the record.
   * @return Returns the record.
   */
  public Record<K> readRecord() throws IOException
  {
    Record<K> record = new Record<K>( recordFormat);
    recordFormat.readRecord( store, record);
    return record;
  }
  
  /**
   * Mark the specified record as garbage.
   * @param position The position of the record.
   */
  public void markGarbage( long position) throws IOException
  {
    store.seek( position);
    recordFormat.markGarbage( store);
  }
  
  /**
   * Compact a region of the database. The region must begin on a record boundary.
   * @param offset The offset of the region to compact.
   * @param length The length of the region to compact.
   */
  public void compact( long offset, long length) throws IOException
  {
    store.seek( offset);
    while( length > 0)
    {
      byte recordHeader = store.readByte();
      long recordLength = store.readLong();
      if ( recordHeader == 0) 
      {
        store.seek( offset);
        Record<K> record = readRecord();
        insert( record.getKey(), record.getContent());
      }
      
      offset += recordLength;
      length -= recordLength;
    }
  }
  
  /**
   * @return Returns the store.
   */
  public IRandomAccessStore getStore()
  {
    return store;
  }
  
  private IRandomAccessStore store;
  private BTree<K> btree;
  private IRecordFormat<K> recordFormat;
}
