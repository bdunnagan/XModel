package org.xmodel.lss;

import java.io.IOException;
import org.xmodel.lss.store.IRandomAccessStore;

/**
 * An interface that defines the structure of a record in the database.
 */
public interface IRecordFormat<K>
{
  public final static int garbageFlag = 0x01;
  public final static int nodeFlag = 0x02;
  public final static int leafFlag = 0x04;
  
  /**
   * @return Returns the implementation of IKeyFormat<K>.
   */
  public IKeyFormat<K> getKeyFormat();
  
  /**
   * Read a key from the current position.
   * @param store The store.
   * @return Returns the key.
   */
  public K readKey( IRandomAccessStore store) throws IOException;

  /**
   * Write a key at the current position.
   * @param store The store.
   * @param key The key.
   */
  public void writeKey( IRandomAccessStore store, K key) throws IOException;
  
  /**
   * Extract the key from the current record and advance to the next record.
   * @param store The store.
   * @return Returns the key, or null if the record was garbage.
   */
  public K extractKey( IRandomAccessStore store) throws IOException;
  
  /**
   * Extract the key from the specified record.
   * @param content The record.
   * @return Returns the key.
   */
  public K extractKey( byte[] content) throws IOException;
  
  /**
   * Read the record header.
   * @param store The store.
   * @param record Returns a record with the header fields set.
   */
  public void readHeader( IRandomAccessStore store, Record record) throws IOException;

  /**
   * Write the record header.
   * @param store The store.
   * @param record A record with the header fields set
   */
  public void writeHeader( IRandomAccessStore store, Record record) throws IOException;
  
  /**
   * Read a record at the current position.
   * @param store The store.
   * @param record Returns the record.
   */
  public void readRecord( IRandomAccessStore store, Record record) throws IOException;

  /**
   * Write a record at the current position.
   * @param store The store.
   * @param content The content of the record.
   */
  public void writeRecord( IRandomAccessStore store, byte[] content) throws IOException;

  /**
   * Read an index node at the current position.
   * @param store The store.
   * @param node Returns the node.
   */
  public void readNode( IRandomAccessStore store, BNode<K> node) throws IOException;

  /**
   * Write an index node from the current position.
   * @param store The store.
   * @param node The node.
   */
  public void writeNode( IRandomAccessStore store, BNode<K> node) throws IOException;

  /**
   * Mark the record at the current position as garbage.
   * @param store The store.
   */
  public void markGarbage( IRandomAccessStore store) throws IOException;

  /**
   * Returns true if the record at the current position is garbage.
   * @param store The store.
   * @return Returns true if the record at the current position is garbage.
   */
  public boolean isGarbage( IRandomAccessStore store) throws IOException;
}
