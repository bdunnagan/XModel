package org.xmodel.mongo;
import org.xmodel.IChangeSet;
import org.xmodel.external.ITransaction;

public class MongoTransaction implements ITransaction
{
  /* (non-Javadoc)
   * @see org.xmodel.external.ITransaction#lock(int)
   */
  @Override
  public boolean lock( int timeout)
  {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ITransaction#unlock()
   */
  @Override
  public void unlock()
  {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ITransaction#commit()
   */
  @Override
  public boolean commit()
  {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ITransaction#rollback()
   */
  @Override
  public boolean rollback()
  {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ITransaction#getChanges()
   */
  @Override
  public IChangeSet getChanges()
  {
    // TODO Auto-generated method stub
    return null;
  }
}
