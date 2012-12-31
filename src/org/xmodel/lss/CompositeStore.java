package org.xmodel.lss;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

/**
 * An IRandomAccessStore that delegates to a collection of IRandomAccessStore instances.  A CompositeStore
 * always has a current delegate, and the CompositeStore's position and length are that of the current
 * delegate.  However, in order to maintain an index across all of the delegate stores, some number of
 * high bits of the position pointer are used to identify the store from which the pointer was allocated.
 * This limits the size of a delegate store to something less than what a long pointer would otherwise
 * allow.  The maximum size of a delegate store versus the number of delegate stores is a tradeoff that
 * is chosen by specifying the maximum size of a store.  Choosing smaller stores will improve performance
 * because there are more stores and therefore more opportunity to find low utility stores to garbage 
 * collect, and a utility score for a smaller store will yield a faster collection time than the same
 * score for a larger store.
 * <p>
 * Only pointers from the current, active store may be compared, since there is no guarantee that a pointer
 * from an older store is less than a pointer from a newer store.  In this sense CompositeStore breaks the
 * general contract of IRandomAccessStore that all pointer positions are comparable.
 * <p>
 * Delegate stores are ordered, which means that writes may span multiple stores.
 */
public class CompositeStore extends AbstractStore
{
  public CompositeStore( List<IRandomAccessStore> stores) throws IOException
  {
    this.stores = new TreeMap<Long, IRandomAccessStore>();
    this.touched = new HashSet<IRandomAccessStore>();
    
    for( IRandomAccessStore store: stores)
      this.stores.put( store.first(), store);
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
    if ( store.position() == store.length()) length++;
    touched.add( store);
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRandomAccessStore#flush()
   */
  @Override
  public void flush() throws IOException
  {
    for( IRandomAccessStore store: touched)
      store.flush();
    touched.clear();
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRandomAccessStore#seek(long)
   */
  @Override
  public void seek( long vpointer) throws IOException
  {
    Entry<Long, IRandomAccessStore> entry = stores.ceilingEntry( vpointer);
    if ( entry == null) throw new IndexOutOfBoundsException();
    store = entry.getValue();
    store.seek( vpointer);
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRandomAccessStore#position()
   */
  @Override
  public long position() throws IOException
  {
    return store.position();
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRandomAccessStore#length()
   */
  @Override
  public long length() throws IOException
  {
    return store.length();
  }

  /**
   * @return Returns the delegate store with the least utility.
   */
  public IRandomAccessStore getLeastUtility()
  {
  }

  private TreeMap<Long, IRandomAccessStore> stores;
  private Set<IRandomAccessStore> touched;
  private IRandomAccessStore store;
}
