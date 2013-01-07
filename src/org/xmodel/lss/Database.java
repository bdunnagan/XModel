package org.xmodel.lss;

import java.io.IOException;
import java.util.Comparator;
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
    
    storageController.finishIndex( indexes);
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
   * Query a record from the database with one or more keys.
   * @param key The key.
   * @return Returns null or the record.
   */
  public byte[] query( K[] keys) throws IOException
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
   * Write the database indexes to the store.
   */
  public void storeIndex() throws IOException
  {
    // begin with clean slate
    storageController.flush();
    
    // update indexes
    for( BTree<K> index: indexes)
      index.root.store();
    
    // update index pointer
    storageController.writeIndexPointer( root.pointer);

    //
    // Mark index garbage. 
    // Failure just before this point could result in leaked garbage.
    //
    while( garbage.size() > 0)
    {
      BNode<K> node = garbage.remove( 0);
      if ( node.pointer > 0) storageController.markGarbage( node.pointer);
    }
    
    // flush changes
    storageController.flush();
  }
  
  private StorageController<K> storageController;
  private List<BTree<K>> indexes;
  private Record record;
}
