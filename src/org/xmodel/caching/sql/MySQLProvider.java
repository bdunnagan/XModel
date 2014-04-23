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
import java.util.Set;

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
    String host = Xlate.childGet( annotation, "host", "localhost");
    url = String.format( "jdbc:mysql://%s/?cachePrepStmts=true&rewriteBatchedStatements=true", host);  
   
    username = Xlate.childGet( annotation, "username", (String)null);
    if ( username == null) throw new CachingException( "Username not defined in annotation: "+annotation);
    
    password = Xlate.childGet( annotation, "password", (String)null);
    if ( password == null) throw new CachingException( "Password not defined in annotation: "+annotation);
    
    database = Xlate.childGet( annotation, "database", (String)null);
    
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
      log.debugf( "JDBC lease: %1.0fus", ((t1-t0)/1e3));
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
   * @see org.xmodel.caching.sql.ISQLProvider#createStatement(java.sql.Connection, java.lang.String, long, long, boolean, boolean, boolean, int[])
   */
  @Override
  public PreparedStatement createStatement( Connection connection, String query, long limit, long offset, boolean stream, boolean readonly, boolean cache, int[] columnTypes) throws SQLException
  {
    if ( cache)
    {
      // check if stored procedure already created (lookup in set)
      // if not, attempt to create stored procedure (may fail)
      // return CallableStatement for stored procedure
    }
    else
    {
      if ( limit >= 0) 
      {
        query = (offset < 0)? 
          String.format( "%s LIMIT %d", query, limit):
          String.format( "%s LIMIT %d OFFSET %d", query, limit, offset);
      }
  
      int resultSetConcur = (stream | readonly)? ResultSet.CONCUR_READ_ONLY: ResultSet.CONCUR_UPDATABLE;
      PreparedStatement statement = connection.prepareStatement( query, ResultSet.TYPE_FORWARD_ONLY, resultSetConcur);
      if ( stream) statement.setFetchSize( Integer.MIN_VALUE);
      
      ConnectionPool.log.debugf( "ConnectionPool [%X] -> %s", connection.hashCode(), statement.toString());
      
      return statement;
    }
  }
  
  private void createStoredProcedure( Connection connection, String query, int[] paramTypes)
  {
    StringBuilder sb = new StringBuilder();
    sb.append( "DELIMITER //\n");
    sb.append( String.format( "CREATE PROCEDURE `%s` (IN %s %s)", procName));
    for( int i=0; i<paramTypes.length; i++)
    {
      sb.append( String.format( " (IN %s %s)", paramNames[ i], getTypeName( paramTypes[ i]))); 
    }
    
    BEGIN
        SELECT var1 + 2 AS result;
    END//
  }
  
  private String getTypeName( int type)
  {
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
  
  private String url;
  private String username;
  private String password;
  private String database;
  private ConnectionPool pool;
  private Set<String> cached;
}
