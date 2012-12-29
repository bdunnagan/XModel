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
    this.record = new Record();
    
    finishIndex( recordFormat);
  }
  
  public Database( BTree<K> btree, IRandomAccessStore store, IRecordFormat<K> recordFormat) throws IOException
  {
    this.store = store;
    this.btree = btree;
    this.recordFormat = recordFormat;
    this.record = new Record();
    
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
      
      recordFormat.readHeader( store, record);
      long advance = store.position() + record.getLength();
      
      store.seek( pointer);
      K key = recordFormat.extractKey( store);
      if ( key != null) btree.insert( key, pointer);
      
      store.seek( advance);
    }
  }
  
  /**
   * Insert a record into the database.
   * @param key The key.
   * @param data The data to associate with the key.
   */
  public void insert( K key, byte[] data) throws IOException
  {
    store.seek( store.length());
    long position = store.position();
    recordFormat.writeRecord( store, data);
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
      recordFormat.readRecord( store, record);
      return record.getContent();
    }
    return null;
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
    long freeStart = offset;
    long end = offset + length;
    while( offset < end)
    {
      try
      {
        store.seek( offset);
        recordFormat.readRecord( store, record);
        offset = store.position();
        if ( !record.isGarbage()) 
        {
          K key = recordFormat.extractKey( record.getContent());
          insert( key, record.getContent());
        }
      }
      catch( IllegalStateException e)
      {
      }
    }
    
    long freeLength = offset - freeStart - 9;
    store.seek( freeStart);

    record.setGarbage( true);
    record.setLength( freeLength);
    recordFormat.writeHeader( store, record);
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
  private Record record;
}
