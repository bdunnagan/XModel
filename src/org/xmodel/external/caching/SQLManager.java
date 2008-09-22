package org.xmodel.external.caching;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.xmodel.IModelObject;
import org.xmodel.external.CachingException;

/**
 * An interface for creating (and possibly caching) instances of java.sql.Statement. An implementation
 * of the interface must be defined on the <i>meta:sqlmanager</i> attribute or <i>meta:sqlmanager</i> child 
 * of an ancestor of each IExternalReference which uses an SQLCachingPolicy.
 */
public interface SQLManager
{
  /**
   * Configure this manager from the specified annotation.
   * @param annotation The annotation.
   */
  public void configure( IModelObject annotation) throws CachingException;
  
  /**
   * Returns a Connection possibly from a pool of Connection objects.
   * @return Returns a Connection possibly from a pool of Connection objects.
   */
  public Connection getConnection() throws CachingException;
  
  /**
   * Returns a PreparedStatement built from the specified SQL. This method is provided to allow
   * the implementation to optionally cache PreparedStatement instances.
   * @param sql The SQL statement.
   * @return Returns a PreparedStatement built from the specified SQL.
   */
  public PreparedStatement prepareStatement( String sql) throws CachingException; 
}
