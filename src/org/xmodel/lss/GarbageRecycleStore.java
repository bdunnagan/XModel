package org.xmodel.lss;

import java.io.IOException;

/**
 * An IRandomAccessStore that is mapped to a garbage record in another instance of IRandomAccessStore. 
 * If a write operation goes beyond the end of the store, and thus beyond the end of the garbage record, 
 * a  defragmentation is triggered ahead of the write operation to make room.
 */
public class GarbageRecycleStore<K> extends AbstractStore
{
  public GarbageRecycleStore( Database<K> db, long start, long end)
  {
    this.db = db;
    this.start = start;
    this.end = end;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.lss.IRandomAccessStore#read(byte[], int, int)
   */
  @Override
  public void read( byte[] bytes, int offset, int length) throws IOException
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRandomAccessStore#write(byte[], int, int)
   */
  @Override
  public void write( byte[] bytes, int offset, int length) throws IOException
  {
    IRandomAccessStore store = db.getStore();
    
    if ( (start + length) > end)
    {
      long position = store.position();
      
      //
      // Store btree at physical end of store.  
      // The store will be up-to-date except for this write.
      //
      store.seek( store.length());
      db.getIndex().store();
      
      //
      // Compact and write non-garbage to physical end of store, so that indexing can be completed
      // if there is a failure before the btree is written again.
      //
      db.compact( end, length);
      
      //
      // Finish this write operation and store the btree again.  Failure to store the btree means
      // this write failed, since it is not at the end of the store and will not be included in
      // the index on reboot.
      //
      
      // HOW TO COMMUNICATE THIS TO IRecordFormat.writeNode and writeRecord?
      
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRandomAccessStore#readByte()
   */
  @Override
  public byte readByte() throws IOException
  {
    return db.getStore().readByte();
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRandomAccessStore#writeByte(byte)
   */
  @Override
  public void writeByte( byte b) throws IOException
  {
    db.getStore().writeByte( b);
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRandomAccessStore#flush()
   */
  @Override
  public void flush() throws IOException
  {
    db.getStore().flush();
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRandomAccessStore#seek(long)
   */
  @Override
  public void seek( long position) throws IOException
  {
    db.getStore().seek( position);
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRandomAccessStore#position()
   */
  @Override
  public long position() throws IOException
  {
    return db.getStore().position();
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRandomAccessStore#length()
   */
  @Override
  public long length() throws IOException
  {
    return db.getStore().length();
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRandomAccessStore#end()
   */
  @Override
  public long end() throws IOException
  {
    return db.getStore().end();
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRandomAccessStore#end(long)
   */
  @Override
  public void end( long position) throws IOException
  {
    db.getStore().end( position);
  }

  private Database<K> db;
  private long start;
  private long end;
}
