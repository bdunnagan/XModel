package org.xmodel.lss;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import org.xmodel.lss.store.IRandomAccessStore;
import org.xmodel.lss.store.IRandomAccessStoreFactory;

public class StorageAllocator
{
  public StorageAllocator( List<IRandomAccessStore> stores, IRandomAccessStoreFactory factory) throws IOException
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

  /**
   * @return Returns the active store to which new records are appended.
   */
  public IRandomAccessStore getActiveStore()
  {
  }

  /**
   * Returns the store that contains the specified pointer.
   * @param pointer The pointer.
   * @return Returns the store that contains the specified pointer.
   */
  public IRandomAccessStore getStore( long pointer)
  {
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
