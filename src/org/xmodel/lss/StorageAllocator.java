package org.xmodel.lss;

import java.io.IOException;
import java.util.ArrayList;
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
  public final static int storeIdentifierOffset = StorageController.headerLength;
  public final static int storeIdentifierLength = 4;
  public final static int headerLength = StorageController.headerLength + storeIdentifierLength;
  
  public StorageAllocator( List<IRandomAccessStore> stores, IRandomAccessStoreFactory factory) throws IOException
  {
    this.factory = factory;
    this.stores = new ArrayList<IRandomAccessStore>( stores);
    this.storeMap = new TreeMap<Short, IRandomAccessStore>();
    this.touched = new HashSet<IRandomAccessStore>();

    for( IRandomAccessStore store: stores)
    {
      store.seek( storeIdentifierOffset);
      short offset = (short)store.readInt();
      storeMap.put( offset, store);
    }
    
    if ( stores.size() == 0) 
    {
      addStore();
    }
    else
    {
      Entry<Short, IRandomAccessStore> entry = storeMap.lastEntry();
      activeID = entry.getKey();
      active = entry.getValue();
    }
  }

  /**
   * @return Returns the active store to which new records are appended.
   */
  public IRandomAccessStore getActiveStore()
  {
    return active;
  }
  
  /**
   * Converts the specified pointer into an absolute pointer for the specified store.
   * @param store The store containing the pointer.
   * @param pointer The pointer.
   * @return Returns an absolute pointer.
   */
  public long getStorePointer( IRandomAccessStore store, long pointer)
  {
    if ( cachedStore != store)
    {
      cachedStore = store;
      cachedStoreID = -1;
      for( Entry<Short, IRandomAccessStore> entry: storeMap.entrySet())
        if ( entry.getValue() == store)
        {
          cachedStoreID = entry.getKey();
          break;
        }
    }
    
    if ( cachedStoreID == -1 || (pointer >> storeBits) != 0) throw new IllegalArgumentException();
    return pointer | (cachedStoreID << storeBits);
  }

  /**
   * Converts the specified pointer into the active store to an absolute pointer.  An absolute
   * pointer includes the store identifier.
   * @param pointer The pointer.
   * @return Returns an absolute pointer.
   */
  public long getActiveStorePointer( long pointer)
  {
    if ( (pointer >> storeBits) != 0) throw new IllegalArgumentException();
    return pointer | (activeID << storeBits);
  }

  /**
   * Returns true if the specified pointer is in the active store.
   * @param pointer The pointer.
   * @return Returns true if the specified pointer is in the active store.
   */
  public boolean isActiveStorePointer( long pointer)
  {
    short storeID = (short)(pointer >> storeBits);
    return storeID == activeID;
  }
  
  /**
   * Returns the store that contains the specified pointer.
   * @param pointer The pointer.
   * @return Returns the store that contains the specified pointer.
   */
  public IRandomAccessStore getStore( long pointer) throws IOException
  {
    short storeID = (short)(pointer >> storeBits);
    
    Entry<Short, IRandomAccessStore> current = storeMap.ceilingEntry( storeID);
    if ( current == null) throw new IndexOutOfBoundsException();
    
    return current.getValue();
  }

  /**
   * Returns the store that contains the specified pointer and seeks to the pointer.
   * @param pointer The pointer.
   * @return Returns the store that contains the specified pointer.
   */
  public IRandomAccessStore getStoreAndSeek( long pointer) throws IOException
  {
    IRandomAccessStore store = getStore( pointer);
    store.seek( pointer & storeMask);
    return store;
  }

  /**
   * Flush changes made to all stores managed by this allocator.
   */
  public void flush() throws IOException
  {
    for( IRandomAccessStore store: touched) store.flush();
    touched.clear();
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
    while( true)
    {
      IRandomAccessStore store = storeMap.get( start);
      
      if ( store == null)
      {
        IRandomAccessStore newStore = factory.createInstance();
        if ( active != null) initNewStoreFromActiveStore( newStore);
        newStore.seek( storeIdentifierOffset);
        newStore.writeInt( start);
        newStore.flush();

        stores.add( newStore);
        storeMap.put( start, newStore);
        active = newStore;
        activeID = start;
        break;
      }
      
      start++;
    }
  }
  
  /**
   * Remove a store from the collection.
   * @param store The store.
   */
  public void removeStore( IRandomAccessStore store) throws IOException
  {
    store.seek( storeIdentifierOffset);
    short start = (short)store.readInt();
    storeMap.remove( start);
  }
  
  /**
   * Initialize a new store using the active store.
   * @param store The store to be initialized.
   */
  private void initNewStoreFromActiveStore( IRandomAccessStore store) throws IOException
  {
    byte[] header = new byte[ StorageController.headerLength];
    active.seek( 0);
    active.read( header, 0, header.length);
    store.write( header, 0, header.length);
  }
  
  /**
   * @return Returns the list of stores.
   */
  public List<IRandomAccessStore> getStores()
  {
    return stores;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    for( Entry<Short, IRandomAccessStore> entry: storeMap.entrySet())
    {
      sb.append( String.format( "Store %d\n", entry.getKey()));
      sb.append( entry.getValue().toString());
      sb.append( "\n\n");
    }
    
    sb.setLength( sb.length() - 1);
    
    return sb.toString();
  }

  private final static long storeBits = 48;
  private final static long storeMask = (1L << storeBits) - 1;
  
  private IRandomAccessStoreFactory factory;
  private List<IRandomAccessStore> stores;
  private TreeMap<Short, IRandomAccessStore> storeMap;
  private Set<IRandomAccessStore> touched;
  private IRandomAccessStore active;
  private long activeID;
  
  private IRandomAccessStore cachedStore;
  private long cachedStoreID;
}
