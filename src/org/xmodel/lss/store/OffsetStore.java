package org.xmodel.lss.store;

import java.io.IOException;

/**
 * An IRandomAccessStore that delegates to another IRandomAccessStore, but interposes a constant
 * offset to the pointer.  The position of the first byte in the store is addressed at the offset
 * position, and the length of the store is the length of the delegate plus the offset.
 */
class OffsetStore extends AbstractStore
{
  public OffsetStore( IRandomAccessStore store, long offset)
  {
    this.store = store;
    this.first = offset;
  }
  
  /**
   * @return Returns the offset of this store.
   */
  public long getOffset()
  {
    return first;
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRandomAccessStore#read(byte[], int, int)
   */
  @Override
  public void read( byte[] bytes, int offset, int length) throws IOException
  {
    store.read( bytes, offset, length);
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRandomAccessStore#write(byte[], int, int)
   */
  @Override
  public void write( byte[] bytes, int offset, int length) throws IOException
  {
    store.write( bytes, offset, length);
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRandomAccessStore#readByte()
   */
  @Override
  public byte readByte() throws IOException
  {
    return store.readByte();
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRandomAccessStore#writeByte(byte)
   */
  @Override
  public void writeByte( byte b) throws IOException
  {
    store.writeByte( b);
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRandomAccessStore#flush()
   */
  @Override
  public void flush() throws IOException
  {
    store.flush();
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRandomAccessStore#seek(long)
   */
  @Override
  public void seek( long position) throws IOException
  {
    store.seek( position - first);
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRandomAccessStore#position()
   */
  @Override
  public long position() throws IOException
  {
    return store.position() + first;
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRandomAccessStore#length()
   */
  @Override
  public long length() throws IOException
  {
    return store.length();
  }

  private IRandomAccessStore store;
  private long first;
}
