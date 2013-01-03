package org.xmodel.lss;

import java.io.IOException;
import java.util.Comparator;

/**
 * Experimental implementation of a log-structured database.
 */
public class Database<K>
{
  public Database( StorageController<K> store) throws IOException
  {
    this( store, null);
  }
  
  public Database( StorageController<K> store, Comparator<K> comparator) throws IOException
  {
    this.store = store;
    this.btree = new BTree<K>( 1000, store, comparator);
    this.record = new Record();
    
    store.finishIndex( btree);
  }
  
  public Database( BTree<K> btree, StorageController<K> store) throws IOException
  {
    this.store = store;
    this.btree = btree;
    this.record = new Record();
    
    store.finishIndex( btree);
  }
  
  /**
   * Insert a record into the database.
   * @param key The key.
   * @param data The data to associate with the key.
   */
  public void insert( K key, byte[] data) throws IOException
  {
    long position = store.writeRecord( data);
    position = btree.insert( key, position);
    if ( position > 0) store.markGarbage( position);
  }
  
  /**
   * Delete a record from the database.
   * @param key The key.
   */
  public void delete( K key) throws IOException
  {
    long position = btree.delete( key);
    if ( position > 0) store.markGarbage( position);
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
      store.readRecord( position, record);
      return record.getContent();
    }
    return null;
  }
  
  private StorageController<K> store;
  private BTree<K> btree;
  private Record record;
}
