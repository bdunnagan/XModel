package org.xmodel.caching.sql;

import java.sql.Connection;
import java.sql.SQLException;

import org.xmodel.external.CachingException;
import org.xmodel.external.IExternalReference;
import org.xmodel.external.ITransaction;
import org.xmodel.log.SLog;

/**
 * An implementation of ITransaction for SQL databases.
 */
public class SQLTransaction implements ITransaction
{
  public SQLTransaction( SQLTableCachingPolicy cachingPolicy)
  {
    this.cachingPolicy = cachingPolicy;
    this.state = State.ready;
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ITransaction#state()
   */
  @Override
  public State state()
  {
    return state;
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ITransaction#track(org.xmodel.external.IExternalReference)
   */
  @Override
  public void track( IExternalReference reference)
  {
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
      state = State.locked;
      return true;
    }
    catch( SQLException e)
    {
      SLog.exception( this, e);
      return false;
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ITransaction#unlock()
   */
  @Override
  public void unlock()
  {
    if ( connection != null)
    {
      try
      {
        connection.setAutoCommit( true);
        state = State.ready;
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
        cachingPolicy.transactionComplete( this);
        connection = null;
      }
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
      state = State.committed;
      return true;
    }
    catch( SQLException e)
    {
      rollback();
      
      SLog.exception( this, e);
      state = State.error;
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
      state = State.rolledback;
      return true;
    }
    catch( SQLException e)
    {
      SLog.exception( this, e);
      state = State.error;
      return false;
    }
  }

  private SQLTableCachingPolicy cachingPolicy;
  private Connection connection;
  private State state;
}
