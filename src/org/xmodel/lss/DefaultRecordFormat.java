package org.xmodel.lss;

import java.io.IOException;

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
    if ( header == 1) return null;

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
    
    record.garbage = header == 1;
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
   * @see org.xmodel.lss.IRecordFormat#markGarbage(org.xmodel.lss.IRandomAccessStore)
   */
  @Override
  public void markGarbage( IRandomAccessStore store) throws IOException
  {
    store.writeByte( (byte)1);
  }
  
  private IKeyFormat<K> keyFormat;
}
