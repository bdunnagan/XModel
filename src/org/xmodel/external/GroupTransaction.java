package org.xmodel.external;

import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of ITransaction that atomically operates on one or more nested instances of ITranscation.
 */
public class GroupTransaction implements ITransaction
{
  public GroupTransaction()
  {
    transactions = new ArrayList<ITransaction>();
    locked = new ArrayList<ITransaction>();
    corrupt = new ArrayList<ITransaction>();
  }
  
  /**
   * Add a transaction to the group.
   * @param transaction The transaction.
   */
  public void addTransaction( ITransaction transaction)
  {
    transactions.add( transaction);
  }
  
  /**
   * Remove a transaction from the group.
   * @param transaction The transaction.
   */
  public void removeTransaction( ITransaction transaction)
  {
    transactions.remove( transaction);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.external.ITransaction#lock(int)
   */
  @Override
  public boolean lock( int timeout)
  {
    for( ITransaction transaction: transactions)
    {
      long time = System.nanoTime();
      if ( transaction.lock( timeout)) locked.add( transaction); else break;
      timeout -= System.nanoTime() - time;
      if ( timeout <= 0) break;
    }
    
    if ( locked.size() != transactions.size())
    {
      unlock();
      return false;
    }
    
    transactions.clear();
    return true;
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ITransaction#free()
   */
  @Override
  public void unlock()
  {
    for( ITransaction transaction: locked)
    {
      transaction.unlock();
      transactions.add( transaction);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ITransaction#commit()
   */
  @Override
  public boolean commit()
  {
    if ( locked.size() == 0 || corrupt.size() > 0) throw new IllegalStateException();

    int commited = 0;
    for( ITransaction transaction: locked)
    {
      try
      {
        if ( !transaction.commit()) break;
        ++commited;
      }
      finally
      {
        corrupt.add( transaction);
      }
    }
    
    if ( commited != locked.size())
    {
      for( int i=0; i<commited; i++)
      {
        ITransaction transaction = locked.get( i);
        try
        {
          if ( !transaction.rollback()) corrupt.add( transaction);
        }
        finally
        {
          corrupt.add( transaction);
        }
      }
    }
    
    return corrupt.size() == 0;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.external.ITransaction#rollback()
   */
  @Override
  public boolean rollback()
  {
    if ( locked.size() == 0 || corrupt.size() > 0) throw new IllegalStateException();
    
    corrupt = new ArrayList<ITransaction>();
    for( ITransaction transaction: locked)
    {
      try
      {
        if ( !transaction.rollback())
          corrupt.add( transaction);
      }
      finally
      {
        corrupt.add( transaction);
      }
    }
    
    return corrupt.size() == 0;
  }
  
  private List<ITransaction> transactions;
  private List<ITransaction> locked;
  private List<ITransaction> corrupt;
}
