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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.xmodel.IModelObject;
import org.xmodel.Xlate;
import org.xmodel.external.CachingException;
import org.xmodel.log.Log;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

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
    String username = Xlate.childGet( annotation, "username", (String)null);
    if ( username == null) throw new CachingException( "Username not defined in annotation: "+annotation);
    
    String password = Xlate.childGet( annotation, "password", (String)null);
    if ( password == null) throw new CachingException( "Password not defined in annotation: "+annotation);
    
    database = Xlate.childGet( annotation, "database", (String)null);
    
    String host = Xlate.childGet( annotation, "host", "localhost");
    String url = String.format( "jdbc:mysql://%s/%s", host, database);
    
    int maxPoolSize = Xlate.childGet( annotation, "maxPoolSize", -1); 
    int minIdleTime = Xlate.childGet( annotation, "minIdleTime", -1); 
       
    HikariConfig config = new HikariConfig();
    config.setPoolName( database);
    if ( maxPoolSize > 0) config.setMaximumPoolSize( maxPoolSize);
    //if ( minIdleTime >= 0) config.setMinimumIdle( minIdleTime);
    config.setDataSourceClassName( "com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
    config.addDataSourceProperty( "url", url);
    config.addDataSourceProperty( "serverName", host);
    config.addDataSourceProperty( "port", "3306");
    config.addDataSourceProperty( "databaseName", database);
    config.addDataSourceProperty( "user", username);
    config.addDataSourceProperty( "password", password);
    config.addDataSourceProperty( "cachePrepStmts", true);
    config.addDataSourceProperty( "prepStmtCacheSize", 250);
    config.addDataSourceProperty( "prepStmtCacheSqlLimit", 2048);
    config.addDataSourceProperty( "useServerPrepStmts", true);
    config.addDataSourceProperty( "rewriteBatchedStatements", true);
    config.setRegisterMbeans( true);

    dataSource = new HikariDataSource( config);
    dataSource.setRegisterMbeans( true);
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
//      Connection connection = DriverManager.getConnection( url, username, password);
//      if ( database != null) connection.setCatalog( database);
      Connection connection = dataSource.getConnection();
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
      return newConnection();
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
    try
    {
      connection.close();
    }
    catch( Exception e)
    {
      log.exception( e);
    }
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
    
    PreparedStatement statement;
    
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
    
    return statement;
  }
    
  /* (non-Javadoc)
   * @see org.xmodel.caching.sql.ISQLProvider#close(java.sql.PreparedStatement)
   */
  @Override
  public void close( PreparedStatement statement)
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

  private final static String driverClassName = "com.mysql.jdbc.Driver";
  private final static Log log = Log.getLog( MySQLProvider.class);
  
  private String database;
  private HikariDataSource dataSource;
}
