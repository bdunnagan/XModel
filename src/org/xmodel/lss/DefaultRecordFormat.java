package org.xmodel.lss;

import java.io.IOException;
import java.util.List;
import org.xmodel.lss.BNode.Entry;

public class DefaultRecordFormat<K> implements IRecordFormat<K>
{
  public DefaultRecordFormat( IKeyFormat<K> keyFormat)
  {
    this.keyFormat = keyFormat;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.lss.IRecordFormat#getKeyFormat()
   */
  @Override
  public IKeyFormat<K> getKeyFormat()
  {
    return keyFormat;
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRecordFormat#readKey(org.xmodel.lss.IRandomAccessStore)
   */
  @Override
  public K readKey( IRandomAccessStore store) throws IOException
  {
    return keyFormat.readKey( store);
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRecordFormat#writeKey(org.xmodel.lss.IRandomAccessStore, java.lang.Object)
   */
  @Override
  public void writeKey( IRandomAccessStore store, K key) throws IOException
  {
    keyFormat.writeKey( store, key);
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRecordFormat#extractKeyAndAdvance(org.xmodel.lss.IRandomAccessStore)
   */
  @Override
  public K extractKeyAndAdvance( IRandomAccessStore store) throws IOException
  {
    byte header = store.readByte();
    long length = store.readLong();
    if ( (header & garbageFlag) != 0) return null;

    // save position of record content
    long position = store.position();
    
    // extract key
    K key = keyFormat.extractKeyFromRecord( store);
    
    // advance to next record
    store.seek( position + length);
    
    return key;
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRecordFormat#extractKeyFromRecord(byte[])
   */
  @Override
  public K extractKeyFromRecord( byte[] content) throws IOException
  {
    return keyFormat.extractKeyFromRecord( content);
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRecordFormat#readRecord(org.xmodel.lss.IRandomAccessStore, org.xmodel.lss.Record)
   */
  @Override
  public void readRecord( IRandomAccessStore store, Record<K> record) throws IOException
  {
    byte header = store.readByte();
    long length = store.readLong();
    byte[] data = new byte[ (int)length];
    store.read( data, 0, data.length);
    
    record.garbage = (header & garbageFlag) != 0;
    record.content = data;
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRecordFormat#writeRecord(org.xmodel.lss.IRandomAccessStore, org.xmodel.lss.Record)
   */
  @Override
  public void writeRecord( IRandomAccessStore store, Record<K> record) throws IOException
  {
    byte[] content = record.getContent();
    store.writeByte( (byte)0);
    store.writeLong( content.length);
    store.write( content, 0, content.length);
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRecordFormat#readNode(org.xmodel.lss.IRandomAccessStore, org.xmodel.lss.BNode)
   */
  @Override
  public void readNode( IRandomAccessStore store, BNode<K> node) throws IOException
  {
    store.seek( node.getPointer());

    node.clearEntries();
    
    byte flags = store.readByte();
    int count = store.readInt();
    boolean leaf = (flags & leafFlag) != 0;
    
    for( int i=0; i<count; i++)
    {
      K key = readKey( store);
      long pointer = store.readLong();
      Entry<K> entry = new Entry<K>( key, pointer);
      node.addEntry( entry);
    }
    
    if ( !leaf)
    {
      for( int i=0; i<=count; i++)
      {
        long childPointer = store.readLong();
        int childCount = store.readInt();
        BNode<K> child = new BNode<K>( node, childPointer, childCount);
        node.addChild( child);
      }
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRecordFormat#writeNode(org.xmodel.lss.IRandomAccessStore, org.xmodel.lss.BNode)
   */
  @Override
  public void writeNode( IRandomAccessStore store, BNode<K> node) throws IOException
  {
    List<Entry<K>> entries = node.getEntries();
    List<BNode<K>> children = node.getChildren();
    
    store.seek( store.length());
    node.setPointer( store.position());

    store.writeByte( (byte)((children.size() > 0)? 0: leafFlag));
    store.writeInt( node.count());
    
    for( int i=0; i<node.count(); i++)
    {
      Entry<K> entry = entries.get( i);
      writeKey( store, entry.getKey());
      store.writeLong( entry.getPointer());
    }

    if ( children.size() > 0)
    {
      for( int i=0; i<=node.count(); i++)
      {
        BNode<K> child = children.get( i);
        store.writeLong( child.getPointer());
        store.writeInt( child.count());
      }
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRecordFormat#markGarbage(org.xmodel.lss.IRandomAccessStore)
   */
  @Override
  public void markGarbage( IRandomAccessStore store) throws IOException
  {
    store.writeByte( (byte)garbageFlag);
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRecordFormat#isGarbage(org.xmodel.lss.IRandomAccessStore)
   */
  @Override
  public boolean isGarbage( IRandomAccessStore store) throws IOException
  {
    return (store.readByte() & garbageFlag) != 0;
  }

  private final static int garbageFlag = 0x01;
  private final static int leafFlag = 0x02;
  
  private IKeyFormat<K> keyFormat;
}
