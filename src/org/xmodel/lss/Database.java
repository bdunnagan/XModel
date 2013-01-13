package org.xmodel.lss;

import java.io.IOException;
import java.util.List;

/**
 * Experimental implementation of a log-structured database.
 */
public class Database<K>
{
  public Database( List<BTree<K>> indexes, StorageController<K> storageController) throws IOException
  {
    this.storageController = storageController;
    this.indexes = indexes;
    this.record = new Record();
    
    storageController.loadIndex( indexes);
  }
  
  /**
   * Insert a record into the database.
   * @param keys The keys for each index.
   * @param data The data to associate with the key.
   */
  public void insert( K[] keys, byte[] data) throws IOException
  {
    long position = storageController.writeRecord( data);
    position = indexes.get( 0).insert( keys[ 0], position);
    if ( position != 0) storageController.markGarbage( position);
    
    storageController.flush();
  }
  
  /**
   * Delete a record from the database.
   * @param keys The keys.
   */
  public void delete( K[] keys) throws IOException
  {
    long position = indexes.get( 0).delete( keys[ 0]);
    if ( position != 0) storageController.markGarbage( position);
    storageController.flush();
    
    for( int i=1; i<keys.length; i++)
    {
      BTree<K> index = indexes.get( i);
      index.delete( keys[ i]);
    }
  }
  
  /**
   * Query a record from the database with one unique key.
   * @param key The unique key.
   * @param index The index of the index, of course.
   * @return Returns null or the record.
   */
  public byte[] query( K key, int index) throws IOException
  {
    BTree<K> btree = indexes.get( index);
    long position = btree.get( key);
    if ( position != 0) 
    {
      storageController.readRecord( position, record);
      return record.getContent();
    }
    return null;
  }
  
  /**
   * Store the current state of the index.  Call this method more frequently to reduce the time it takes
   * to initialize the database at startup at the expense of increased storage consumption.
   */
  public void storeIndex() throws IOException
  {
    storageController.storeIndex( indexes);
  }
  
  private StorageController<K> storageController;
  private List<BTree<K>> indexes;
  private Record record;
}
