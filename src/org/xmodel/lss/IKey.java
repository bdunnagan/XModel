package org.xmodel.lss;

import java.io.IOException;

import org.xmodel.lss.store.IRandomAccessStore;

public interface IKey extends Comparable<IKey>
{
  /**
   * @return Returns the key data.
   */
  public Object getData();
  
  /**
   * Read the key data from the current position in the specified store.
   * @param store The store.
   */
  public void read( IRandomAccessStore store) throws IOException;
  
  /**
   * Write the key data to the current position in the specified store.
   * @param store The store.
   */
  public void write( IRandomAccessStore store) throws IOException;
}
