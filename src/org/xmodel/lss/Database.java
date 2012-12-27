package org.xmodel.lss;

import java.io.IOException;
import java.util.Comparator;

/**
 * Experimental implementation of a log-structured database.
 */
public class Database<K>
{
  public Database( IRandomAccessStore store, IKeyFormat<K> keyFormat) throws IOException
  {
    this( store, keyFormat, null);
  }
  
  public Database( IRandomAccessStore store, IKeyFormat<K> keyFormat, Comparator<K> comparator) throws IOException
  {
    this.store = store;
    this.btree = new BTree<K>( 1000, keyFormat, store, comparator);
    finishIndex( keyFormat);
  }
  
  public Database( BTree<K> btree, IRandomAccessStore store, IKeyFormat<K> keyFormat) throws IOException
  {
    this.store = store;
    this.btree = btree;
    finishIndex( keyFormat);
  }
  
  /**
   * Read and index the records that follow the last b+tree root in the database.
   * @param keyFormat The key format.
   */
  protected void finishIndex( IKeyFormat<K> keyFormat) throws IOException
  {
    while( true)
    {
      long pointer = store.position();
      if ( pointer >= store.length()) break;
      
      byte header = store.readByte();
      if ( header == 0)
      {
        long length = store.readLong();
        
        // TODO: make key accessible without having to read the entire record?
        byte[] record = new byte[ (int)length];
        store.read( record, 0, record.length);

        // insert may cause seek so store position
        long position = store.position();
        
        K key = keyFormat.extract( record);
        btree.insert( key, pointer);
        
        // restore position
        store.seek( position);
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
    writeRecord( record);
    position = btree.insert( key, position);
    if ( position > 0) trash( position);
  }
  
  /**
   * Delete a record from the database.
   * @param key The key.
   */
  public void delete( K key) throws IOException
  {
    long position = btree.delete( key);
    if ( position > 0) trash( position);
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
      return readRecord();
    }
    return null;
  }
  
  /**
   * Write a record.
   * @param record The record.
   */
  public void writeRecord( byte[] record) throws IOException
  {
    store.writeByte( (byte)0);
    store.writeLong( record.length);
    store.write( record, 0, record.length);
  }
  
  /**
   * Read the record.
   * @return Returns the record.
   */
  public byte[] readRecord() throws IOException
  {
    @SuppressWarnings("unused")
    byte header = store.readByte();
    long length = store.readLong();
    byte[] data = new byte[ (int)length];
    store.read( data, 0, data.length);
    return data;
  }
  
  /**
   * Mark the specified record as garbage.
   * @param position The position of the record.
   */
  public void trash( long position) throws IOException
  {
    store.seek( position);
    store.writeByte( (byte)1);
  }
  
  /**
   * Compact a region of the database. The region must begin on a record boundary.
   * @param keyFormat The key parser.
   * @param offset The offset of the region to compact.
   * @param length The length of the region to compact.
   */
  public void compact( IKeyFormat<K> keyFormat, long offset, long length) throws IOException
  {
    store.seek( offset);
    while( length > 0)
    {
      byte recordHeader = store.readByte();
      long recordLength = store.readLong();
      if ( recordHeader == 0) 
      {
        store.seek( offset);
        byte[] record = readRecord();
        K key = keyFormat.extract( record);
        insert( key, record);
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
}
