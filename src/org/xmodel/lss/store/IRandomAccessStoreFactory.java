package org.xmodel.lss.store;

import java.io.IOException;

/**
 * A factory interface for IRandomAccessStore instances.
 */
public interface IRandomAccessStoreFactory
{
  /**
   * @param id The store identifier.
   * @return Returns a new instance of IRandomAccessStore.
   */
  public IRandomAccessStore createInstance( int id) throws IOException;
}
