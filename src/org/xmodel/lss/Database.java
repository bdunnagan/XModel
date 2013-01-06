package org.xmodel.lss;

import java.io.IOException;
import java.util.Comparator;

/**
 * Experimental implementation of a log-structured database.
 */
public class Database<K>
{
  public Database( StorageController<K> storageController) throws IOException
  {
    this( storageController, null);
  }
  
  public Database( StorageController<K> storageController, Comparator<K> comparator) throws IOException
  {
    this.storageController = storageController;
    this.btree = new BTree<K>( 1000, storageController, comparator);
    this.record = new Record();
    
    storageController.finishIndex( btree);
  }
  
  public Database( BTree<K> btree, StorageController<K> storageController) throws IOException
  {
    this.storageController = storageController;
    this.btree = btree;
    this.record = new Record();
    
    storageController.finishIndex( btree);
  }
  
  /**
   * Insert a record into the database.
   * @param key The key.
   * @param data The data to associate with the key.
   */
  public void insert( K key, byte[] data) throws IOException
  {
    long position = storageController.writeRecord( data);
    position = btree.insert( key, position);
    if ( position != 0) storageController.markGarbage( position);
    storageController.flush();
  }
  
  /**
   * Delete a record from the database.
   * @param key The key.
   */
  public void delete( K key) throws IOException
  {
    long position = btree.delete( key);
    if ( position != 0) storageController.markGarbage( position);
    storageController.flush();
  }
  
  /**
   * Query a record from the database.
   * @param key The key.
   * @return Returns null or the record.
   */
  public byte[] query( K key) throws IOException
  {
    long position = btree.get( key);
    if ( position != 0) 
    {
      storageController.readRecord( position, record);
      return record.getContent();
    }
    return null;
  }
  
  /**
   * Write the index to storage.
   */
  public void storeIndex() throws IOException
  {
    btree.store();
  }
  
  private StorageController<K> storageController;
  private BTree<K> btree;
  private Record record;
}
