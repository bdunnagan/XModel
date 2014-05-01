/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * MySQLManager.java
 * 
 * Copyright 2009 Robert Arvin Dunnagan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.xmodel.caching.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.xmodel.IModelObject;
import org.xmodel.Xlate;
import org.xmodel.external.CachingException;
import org.xmodel.log.Log;

/**
 * An implementation of ISQLProvider for the MySQL database.
 */
public class MySQLProvider implements ISQLProvider
{
  public MySQLProvider() throws ClassNotFoundException
  {
    Class.forName( driverClassName);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.external.caching.SQLCachingPolicy.SQLManager#configure(org.xmodel.IModelObject)
   */
  public void configure( IModelObject annotation) throws CachingException
  {
    cache = new BoundedStatementCache( Xlate.childGet( annotation, "cacheSize", 4096));
    
    username = Xlate.childGet( annotation, "username", (String)null);
    if ( username == null) throw new CachingException( "Username not defined in annotation: "+annotation);
    
    password = Xlate.childGet( annotation, "password", (String)null);
    if ( password == null) throw new CachingException( "Password not defined in annotation: "+annotation);
    
    database = Xlate.childGet( annotation, "database", (String)null);
    
    String host = Xlate.childGet( annotation, "host", "localhost");
    url = String.format( "jdbc:mysql://%s/%s?cachePrepStmts=true&rewriteBatchedStatements=true", host, database);  
   
    int minPoolSize = Xlate.childGet( annotation, "minPoolSize", 20);
    int maxPoolSize = Xlate.childGet( annotation, "maxPoolSize", 80);
    int minPoolWait = Xlate.childGet( annotation, "minPoolWait", 5000);
    int maxPoolWait = Xlate.childGet( annotation, "maxPoolWait", 30000);
    
    pool = new ConnectionPool( this, minPoolSize, maxPoolSize, minPoolWait, maxPoolWait);
  }

  /* (non-Javadoc)
   * @see org.xmodel.caching.sql.ISQLProvider#getDatabase()
   */
  @Override
  public String getDatabase()
  {
    return database;
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.sql.ISQLProvider#newConnection()
   */
  public Connection newConnection() throws CachingException
  {
    try
    {
      Connection connection = DriverManager.getConnection( url, username, password);
      if ( database != null) connection.setCatalog( database);
      return connection;
    }
    catch( Exception e)
    {
      throw new CachingException( "Unable to open connection.", e);
    }
  }  
  
  /* (non-Javadoc)
   * @see org.xmodel.external.sql.ISQLProvider#leaseConnection()
   */
  @Override
  public Connection leaseConnection()
  {
    long t0 = System.nanoTime();
    try
    {
      return pool.lease();
    }
    finally
    {
      long t1 = System.nanoTime();
      log.verbosef( "JDBC lease: %1.0fus", ((t1-t0)/1e3));
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.sql.ISQLProvider#returnConnection(java.sql.Connection)
   */
  @Override
  public void releaseConnection( Connection connection)
  {
    pool.release( connection);
  }

  /* (non-Javadoc)
   * @see org.xmodel.caching.sql.ISQLProvider#createStatement(java.sql.Connection, java.lang.String, long, long, boolean, boolean)
   */
  @Override
  public PreparedStatement createStatement( Connection connection, String query, long limit, long offset, boolean stream, boolean readonly) throws SQLException
  {
    // implement db-specific limit and offset
    if ( limit >= 0) 
    {
      query += " LIMIT " + limit;
      if ( offset >= 0) query += " OFFSET " + offset;
    }

    // configure for read-only and/or streaming
    int resultSetConcur = (stream | readonly)? ResultSet.CONCUR_READ_ONLY: ResultSet.CONCUR_UPDATABLE;
    
    // check cache
    String key = query + resultSetConcur;
    PreparedStatement statement = cache.get( key);
    if ( statement == null)
    {
      // distinguish stored procedure call from query or update
      if ( query.charAt( 0) == '{')
      {
        statement = connection.prepareCall( query, ResultSet.TYPE_FORWARD_ONLY, resultSetConcur);
        if ( stream) statement.setFetchSize( Integer.MIN_VALUE);
      }
      else
      {
        statement = connection.prepareStatement( query, ResultSet.TYPE_FORWARD_ONLY, resultSetConcur);
        if ( stream) statement.setFetchSize( Integer.MIN_VALUE);
      }
      
      if ( cache != null) cache.put( key, statement);
    }
    
    return statement;
  }
    
  /* (non-Javadoc)
   * @see org.xmodel.caching.sql.ISQLProvider#close(java.sql.PreparedStatement)
   */
  @Override
  public void close( PreparedStatement statement)
  {
    if ( cache == null)
    {
      try
      {
       statement.close();
      }
      catch( SQLException e)
      {
        log.exception( e);
      }
    }
  }

  private final static String driverClassName = "com.mysql.jdbc.Driver";
  private final static Log log = Log.getLog( MySQLProvider.class);
  
  private String url;
  private String username;
  private String password;
  private String database;
  private ConnectionPool pool;
  private BoundedStatementCache cache;
}
