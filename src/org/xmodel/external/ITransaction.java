package org.xmodel.external;

/**
 * An interface for a transaction involving one or more IExternalReference instances. 
 */
public interface ITransaction
{
  /**
   * Lock resources in this transaction.
   * @param timeout The timeout in milliseconds.
   * @return Returns true if the lock was acquired, false if timeout expires.
   */
  public boolean lock( int timeout);
  
  /**
   * Unlock resources in this transaction.
   */
  public void unlock();
  
  /**
   * Commit changes to resources in this transaction. Locks must have already been acquired 
   * by calling the <code>lock( int)</code> method, otherwise the commit will fail. This 
   * method will return true if, and only if, the resources were successfully updated.  
   * Similarly, this method will return false if, and only if, the state of the resources
   * has been left unchanged.
   * @return Returns true if the commit succeeded.
   * @throws CachingException If the state of any resource is left in an unknown state.
   */
  public boolean commit();

  /**
   * Revert changes to resources in this transaction following a call to the 
   * <code>commit</code> method. This method will return true if, and only if, 
   * the resources were successfully restored to their state prior to the call
   * to <code>commit</code>. Similarly, this method will return false if, and 
   * only if, the state of the resources has been left unchanged.
   * @return Returns true if the rollback succeeded.
   * @throws CachingException If the state of any resource is left in an unknown state.
   */
  public boolean rollback();
}
