package org.xmodel.lss.store;

/**
 * A factory interface for IRandomAccessStore instances.
 */
public interface IRandomAccessStoreFactory
{
  /**
   * @return Returns a new instance of IRandomAccessStore.
   */
  public IRandomAccessStore createInstance();
}
