package org.xmodel.lss;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
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
 * <p>
 * The following constraints must hold at all times when using a CompositeStore:
 * <ul>
 * <li>Pointers are unique across the collection.
 * <li>Only the last store in the collection may be extended in length.</li>
 * <li>Only comparisons between pointers in the same store are supported.</li>
 * </ul>
 */
public class CompositeStore extends AbstractStore
{
  public CompositeStore( List<IRandomAccessStore> stores) throws IOException
  {
    this.stores = new TreeMap<Long, IRandomAccessStore>();
    this.touched = new HashSet<IRandomAccessStore>();
    
    long start = 0;
    for( IRandomAccessStore store: stores)
    {
      start += (1 << 52);
      this.stores.put( start, new OffsetStore( store, start));
    }
  }
  
  /**
   * Add a new store at the end of the list of stores.
   * @param store The store.
   */
  public void addStore( IRandomAccessStore store)
  {
  }
  
  /**
   * Remove a store from the collection.
   * @param store The store.
   */
  public void removeStore( IRandomAccessStore store)
  {
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
   * @see org.xmodel.lss.IRandomAccessStore#first()
   */
  @Override
  public long first() throws IOException
  {
    return 0;
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
    return stores.lastEntry().getValue().length();
  }

  /**
   * @return Returns the delegate store with the least utility.
   */
  public IRandomAccessStore getLeastUtility() throws IOException
  {
    double minUtility = Double.MAX_VALUE;
    IRandomAccessStore store = null;
    for( Entry<Long, IRandomAccessStore> entry: stores.entrySet())
    {
      double utility = entry.getValue().utility();
      if ( utility < minUtility) 
      {
        utility = minUtility;
        store = entry.getValue();
      }
    }
    return store;
  }

  private TreeMap<Long, IRandomAccessStore> stores;
  private Set<IRandomAccessStore> touched;
  private IRandomAccessStore store;
}
