package org.xmodel.lss.store;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import org.xmodel.lss.BTree;

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
public class CompositeStore extends AbstractStore
{
  public CompositeStore( List<IRandomAccessStore> stores, IRandomAccessStoreFactory factory) throws IOException
  {
    this.factory = factory;
    this.storeMap = new TreeMap<Short, IRandomAccessStore>();
    this.touched = new HashSet<IRandomAccessStore>();

    for( IRandomAccessStore store: stores)
    {
      store.seek( 0);
      short offset = (short)store.readInt();
      storeMap.put( offset, store);
    }
    
    if ( stores.size() == 0) addStore();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.lss.IRandomAccessStore#read(byte[], int, int)
   */
  @Override
  public void read( byte[] bytes, int offset, int length) throws IOException
  {
    current.getValue().read( bytes, offset, length);
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRandomAccessStore#write(byte[], int, int)
   */
  @Override
  public void write( byte[] bytes, int offset, int length) throws IOException
  {
    current.getValue().write( bytes, offset, length);
    touched.add( current.getValue());
    
    //
    // Only test whether the length threshold has increased 
    // because only the most recent store should grow.
    //
    if ( current.getValue().length() > lengthThreshold)
    {
      addStore();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRandomAccessStore#readByte()
   */
  @Override
  public byte readByte() throws IOException
  {
    return current.getValue().readByte();
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRandomAccessStore#writeByte(byte)
   */
  @Override
  public void writeByte( byte b) throws IOException
  {
    current.getValue().writeByte( b);
    touched.add( current.getValue());
    
    //
    // Only test whether the length threshold has increased 
    // because only the most recent store should grow.
    //
    if ( current.getValue().length() > lengthThreshold)
    {
      addStore();
    }
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
    short storeIndex = (short)(pointer >> storeBits);
    Entry<Short, IRandomAccessStore> current = storeMap.ceilingEntry( storeIndex);
    if ( current == null) throw new IndexOutOfBoundsException();
    current.getValue().seek( pointer & storeMask);
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRandomAccessStore#position()
   */
  @Override
  public long position() throws IOException
  {
    return (((long)(current.getKey())) << storeBits) + current.getValue().position();
  }

  /* (non-Javadoc)
   * @see org.xmodel.lss.IRandomAccessStore#length()
   */
  @Override
  public long length() throws IOException
  {
    Entry<Short, IRandomAccessStore> lastEntry = storeMap.lastEntry();
    return (((long)(lastEntry.getKey())) << storeBits) + lastEntry.getValue().length();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.lss.store.AbstractStore#utility()
   */
  @Override
  public double utility() throws IOException
  {
    double sum = 0;
    
    Collection<Entry<Short, IRandomAccessStore>> entries = storeMap.entrySet();
    for( Entry<Short, IRandomAccessStore> entry: entries)
    {
      double utility = entry.getValue().utility();
      sum += utility;
    }
    
    return sum / entries.size();
  }
  
  /**
   * @return Returns the region with the lowest utility.
   */
  public IRandomAccessStore getLowestUtilityRegion() throws IOException
  {
    IRandomAccessStore lowestUtility = null;
    double min = Double.MAX_VALUE;
    
    Collection<Entry<Short, IRandomAccessStore>> entries = storeMap.entrySet();
    for( Entry<Short, IRandomAccessStore> entry: entries)
    {
      double utility = entry.getValue().utility();
      if ( utility < min) 
      {
        min = utility;
        lowestUtility = entry.getValue();
      }
    }
    
    return lowestUtility;
  }

  /**
   * Add a new store at the end of the list of stores.
   * @param newStore The store.
   */
  public void addStore() throws IOException
  {
    short start = 0;
    Entry<Short, IRandomAccessStore> entry = storeMap.firstEntry();
    while( true)
    {
      if ( entry == null || entry.getKey() > start)
      {
        IRandomAccessStore newStore = factory.createInstance();
        newStore.seek( BTree.firstRecordOffset());
        newStore.writeInt( start);
        newStore.flush();
        
        storeMap.put( start, newStore);
        break;
      }
      start++;
    }
        
    current = storeMap.ceilingEntry( start);
  }
  
  /**
   * Remove a store from the collection.
   * @param store The store.
   */
  public void removeStore( IRandomAccessStore store) throws IOException
  {
    store.seek( BTree.firstRecordOffset());
    short start = (short)store.readInt();
    storeMap.remove( start);
  }
  
  /**
   * @return Returns the list of stores.
   */
  public Collection<IRandomAccessStore> getRegions()
  {
    return storeMap.values();
  }
  
  private final static long lengthThreshold = 200L;
  private final static long storeBits = 48;
  private final static long storeMask = (1L << storeBits) - 1;
  
  private IRandomAccessStoreFactory factory;
  private TreeMap<Short, IRandomAccessStore> storeMap;
  private Set<IRandomAccessStore> touched;
  private Entry<Short, IRandomAccessStore> current;
}
