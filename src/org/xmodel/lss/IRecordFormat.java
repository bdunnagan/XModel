package org.xmodel.lss;

import java.io.IOException;

public interface IRecordFormat<K>
{
  public IKeyFormat<K> getKeyFormat();
  
  public K readKey( IRandomAccessStore store) throws IOException;
  
  public void writeKey( IRandomAccessStore store, K key) throws IOException;
  
  public K extractKeyAndAdvance( IRandomAccessStore store) throws IOException;
  
  public K extractKeyFromRecord( byte[] content) throws IOException;
  
  public void readRecord( IRandomAccessStore store, Record<K> record) throws IOException;
  
  public void writeRecord( IRandomAccessStore store, Record<K> record) throws IOException;
  
  public void readNode( IRandomAccessStore store, BNode<K> node) throws IOException;
  
  public void writeNode( IRandomAccessStore store, BNode<K> node) throws IOException;
  
  public void markGarbage( IRandomAccessStore store) throws IOException;
  
  public boolean isGarbage( IRandomAccessStore store) throws IOException;
}
