package org.xmodel.lss;

import java.util.Comparator;

public class Database<K>
{
  public Database( IRandomAccessStore<K> store, IKeyParser<K> parser, Comparator<K> comparator)
  {
    this.store = store;
    this.btree = new BTree<K>( 1000, store, comparator);
    finishIndex( parser);
  }
  
  /**
   * Read and index the records that follow the last b+tree root in the database.
   * @param parser The key parser.
   */
  protected void finishIndex( IKeyParser<K> parser)
  {
    long position = store.position();
    while( position < store.length())
    {
      byte[] record = read();
      K key = parser.extract( record);
      insert( key, record);
    }
  }
  
  /**
   * Insert a record into the database.
   * @param key The key.
   * @param record The record.
   */
  public void insert( K key, byte[] record)
  {
    store.seek( store.length());
    long position = store.position();
    write( record);
    position = btree.insert( key, position);
    if ( position > 0) trash( position);
  }
  
  /**
   * Delete a record from the database.
   * @param key The key.
   */
  public void delete( K key)
  {
    long position = btree.delete( key);
    if ( position > 0) trash( position);
  }
  
  /**
   * Query a record from the database.
   * @param key The key.
   * @return Returns null or the record.
   */
  public byte[] query( K key)
  {
    long position = btree.get( key);
    if ( position > 0) 
    {
      store.seek( position);
      return read();
    }
    return null;
  }
  
  /**
   * Write a record.
   * @param record The record.
   */
  public void write( byte[] record)
  {
    store.writeByte( (byte)0);
    store.writeLong( record.length);
    store.write( record, 0, record.length);
  }
  
  /**
   * Read the record.
   * @return Returns the record.
   */
  public byte[] read()
  {
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
  public void trash( long position)
  {
    store.seek( position);
    store.writeByte( (byte)1);
  }
  
  private IRandomAccessStore<K> store;
  private BTree<K> btree;
}
