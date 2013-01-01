package org.xmodel.lss.store;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import org.xmodel.lss.Database;

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
 * <li>Only comparisons between pointers in the same store have meaning.</li>
 * </ul>
 */
public class CompositeStore<K> extends AbstractStore
{
  public CompositeStore( Database<K> database, List<IRandomAccessStore> stores, IRandomAccessStoreFactory factory) throws IOException
  {
    this.database = database;
    this.factory = factory;
    this.storeMap = new TreeMap<Long, OffsetStore>();
    this.touched = new HashSet<IRandomAccessStore>();
    
    long start = 0;
    for( IRandomAccessStore store: stores)
    {
      storeMap.put( start, new OffsetStore( store, start));
      start += (1 << 52);
    }
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
  public void seek( long pointer) throws IOException
  {
    Entry<Long, OffsetStore> entry = storeMap.ceilingEntry( pointer);
    if ( entry == null) throw new IndexOutOfBoundsException();
    store = entry.getValue();
    store.seek( pointer);
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
    OffsetStore lastStore = storeMap.lastEntry().getValue();
    return lastStore.getOffset() + lastStore.length();
  }
  
  /**
   * Perform garbage collection if necessary.
   */
  private void collect() throws IOException
  {
    OffsetStore store = storeMap.lastEntry().getValue();
    if ( store.length() > storeThreshold)
    {
      IRandomAccessStore leastUtility = getLeastUtility();
      database.compact( store, store.getOffset(), store.length());
    }
  }

  /**
   * Add a new store at the end of the list of stores.
   * @param newStore The store.
   */
  private void addStore()
  {
    long start = 0;
    for( Entry<Long, OffsetStore> entry: storeMap.entrySet())
    {
      if ( entry.getKey() > start)
      {
        storeMap.put( start, new OffsetStore( factory.createInstance(), start));
        return;
      }
      start += (1 << 52);
    }
  }
  
  /**
   * Remove a store from the collection.
   * @param store The store.
   */
  private void removeStore( IRandomAccessStore store)
  {
    if ( !(store instanceof OffsetStore)) throw new IllegalArgumentException();
    storeMap.remove( ((OffsetStore)store).getOffset());
  }
  
  /**
   * @return Returns the delegate store with the least utility.
   */
  public IRandomAccessStore getLeastUtility() throws IOException
  {
    double minUtility = Double.MAX_VALUE;
    IRandomAccessStore store = null;
    for( Entry<Long, OffsetStore> entry: storeMap.entrySet())
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

  private final long storeThreshold = (long)200;
  
  private Database<K> database;
  private IRandomAccessStoreFactory factory;
  private TreeMap<Long, OffsetStore> storeMap;
  private Set<IRandomAccessStore> touched;
  private IRandomAccessStore store;
}
