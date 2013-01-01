package org.xmodel.lss;

import java.io.IOException;
import java.util.List;
import org.xmodel.lss.BNode.Entry;
import org.xmodel.lss.store.IRandomAccessStore;

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
   * @see org.xmodel.lss.IRecordFormat#extractKey(org.xmodel.lss.IRandomAccessStore)
   */
  @Override
  public K extractKey( IRandomAccessStore store) throws IOException
  {
    byte flags = store.readByte();
    long length = store.readLong();
    if ( (flags & (nodeFlag | garbageFlag)) != 0) return null;

    // extract key
    K key = keyFormat.extractKeyFromRecord( store, length);
    
    return key;
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRecordFormat#extractKey(byte[])
   */
  @Override
  public K extractKey( byte[] content) throws IOException
  {
    return keyFormat.extractKeyFromRecord( content);
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRecordFormat#readHeader(org.xmodel.lss.IRandomAccessStore, org.xmodel.lss.Record)
   */
  @Override
  public void readHeader( IRandomAccessStore store, Record record) throws IOException
  {
    record.setFlags( store.readByte());
    record.setLength( store.readLong());
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRecordFormat#writeHeader(org.xmodel.lss.IRandomAccessStore, org.xmodel.lss.Record)
   */
  @Override
  public void writeHeader( IRandomAccessStore store, Record record) throws IOException
  {
    store.writeByte( record.getFlags());
    store.writeLong( record.getLength());
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRecordFormat#readRecord(org.xmodel.lss.IRandomAccessStore, org.xmodel.lss.Record)
   */
  @Override
  public void readRecord( IRandomAccessStore store, Record record) throws IOException
  {
    byte flags = store.readByte();
    long length = store.readLong();
    
    record.setFlags( flags);
    
    if ( (flags & (nodeFlag | leafFlag)) == 0)
    {
      byte[] data = new byte[ (int)length];
      store.read( data, 0, data.length);
      record.setContent( data);
    }
    else
    {
      store.seek( store.position() + length);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRecordFormat#writeRecord(org.xmodel.lss.IRandomAccessStore, byte[])
   */
  @Override
  public void writeRecord( IRandomAccessStore store, byte[] content) throws IOException
  {
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
    if ( (flags & nodeFlag) == 0) throw new IllegalStateException( "Record is not an index node.");
    
    store.readLong();
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

    store.writeByte( (byte)((children.size() > 0)? nodeFlag: (nodeFlag | leafFlag)));
    long lengthPos = store.position();
    store.writeLong( 0);
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
    
    long position = store.position();
    store.seek( lengthPos);
    store.writeLong( position - lengthPos);
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRecordFormat#markGarbage(org.xmodel.lss.IRandomAccessStore)
   */
  @Override
  public void markGarbage( IRandomAccessStore store) throws IOException
  {
    long position = store.position();
    store.writeByte( (byte)garbageFlag);
    long length = store.readLong();
    store.garbage( position, length + 9);
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
  private final static int nodeFlag = 0x04;
  
  private IKeyFormat<K> keyFormat;
}
