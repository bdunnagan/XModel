package org.xmodel.lss;

import java.io.IOException;
import java.util.List;

import org.xmodel.lss.BNode.Entry;
import org.xmodel.lss.BNode.SearchMode;

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
    
    for( int i=1; i<keys.length; i++)
    {
      BTree<K> index = indexes.get( i);
      index.insert( keys[ i], position);
    }
    
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
    
    for( int i=1; i<keys.length; i++)
    {
      BTree<K> index = indexes.get( i);
      index.delete( keys[ i]);
    }
    
    storageController.flush();
  }
  
  /**
   * Delete a record from the database.
   * @param key The key.
   * @param index The index of the b+tree to which the key belongs.
   */
  public void delete( K key, int index) throws IOException
  {
    BTree<K> btree = indexes.get( index);
    long position = btree.get( key);
    if ( position != 0) 
    {
      // keys may be null after restart because b+tree is missing deletes
      K[] keys = storageController.extractKeys( position);
      if ( keys != null) delete( keys);
    }
  }
  
  /**
   * Search for a record from the database with one unique key.
   * @param key The key.
   * @param index The index of the b+tree to which the key belongs.
   * @return Returns null or the record.
   */
  public byte[] search( K key, int index) throws IOException
  {
    BTree<K> btree = indexes.get( index);
    long position = btree.get( key);
    if ( position != 0) 
    {
      storageController.readRecord( position, record);
      
      // b+tree may not be up-to-date with deletes after restart
      if ( record.isGarbage())
      {
        btree.delete( key);
        return null;
      }
      
      return record.getContent();
    }
    return null;
  }

  /**
   * Create a cursor beginning with the key that matches the specified search mode.
   * @param mode The search mode. 
   * @param key The target key.
   * @param index The index of the b+tree to which the key belongs.
   * @param unique True if the key is unique.
   * @return Returns a cursor at the nearest record.
   */
  public BTreeIterator<K> cursor( SearchMode mode, K key, int index, boolean unique) throws IOException
  {
    BTree<K> btree = indexes.get( index);
    BTreeIterator<K> cursor = btree.getCursor( mode, key, unique);
    
    // TODO: Is this correct???
    Entry<K> entry = cursor.get();
    if ( entry != null)
    {
      long position = entry.getPointer();
      storageController.readRecord( position, record);
      
      // b+tree may not be up-to-date with deletes after restart
      if ( record.isGarbage())
      {
        btree.delete( key);
        return null;
      }
    }
    
    return cursor;
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
