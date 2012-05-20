package org.xmodel.external.sql;

import java.sql.Connection;
import java.sql.SQLException;
import org.xmodel.external.CachingException;
import org.xmodel.external.ITransaction;
import org.xmodel.log.SLog;

/**
 * An implementation of ITransaction for SQL databases.
 */
public class SQLTransaction implements ITransaction
{
  public SQLTransaction( SQLDirectCachingPolicy cachingPolicy)
  {
    this.cachingPolicy = cachingPolicy;
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ITransaction#lock(int)
   */
  @Override
  public boolean lock( int timeout)
  {
    try
    {
      connection = cachingPolicy.getSQLProvider().leaseConnection();
      connection.setAutoCommit( false);
      return true;
    }
    catch( SQLException e)
    {
      SLog.errorf( this, e.getMessage());
      return false;
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ITransaction#unlock()
   */
  @Override
  public void unlock()
  {
    try
    {
      connection.setAutoCommit( false);
    }
    catch( SQLException e)
    {
      SLog.error( this, e.getMessage());
      
      throw new CachingException( String.format(
          "Failed to restore database to auto commit mode: %s",
          connection), e);
    }
    finally
    {
      cachingPolicy.getSQLProvider().releaseConnection( connection);
      connection = null;
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ITransaction#commit()
   */
  @Override
  public boolean commit()
  {
    try
    {
      cachingPolicy.commit( connection);
      connection.commit();
      return true;
    }
    catch( SQLException e)
    {
      rollback();
      
      SLog.error( this, e.getMessage());
      return false;
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ITransaction#rollback()
   */
  @Override
  public boolean rollback()
  {
    try
    {
      connection.rollback();
      return true;
    }
    catch( SQLException e)
    {
      SLog.error( this, e.getMessage());
      return false;
    }
  }

  private SQLDirectCachingPolicy cachingPolicy;
  private Connection connection;
}
