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
 * An implementation of ISQLProvider for the Cassandra database.
 */
public class CassandraProvider implements ISQLProvider
{
  public CassandraProvider() throws ClassNotFoundException
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
    url = String.format( "jdbc:cassandra://%s:9160/%s", host, database);
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
      return DriverManager.getConnection( url);      
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
   * @see org.xmodel.caching.sql.ISQLProvider#createStatement(java.sql.Connection, java.lang.String, long, boolean, boolean)
   */
  @Override
  public PreparedStatement createStatement( Connection connection, String query, long limit, boolean stream, boolean readonly) throws SQLException
  {
    // implement db-specific limit and offset
    if ( limit >= 0) query += " LIMIT " + limit;

    // configure for read-only and/or streaming
    int resultSetConcur = (stream | readonly)? ResultSet.CONCUR_READ_ONLY: ResultSet.CONCUR_UPDATABLE;
    
    PreparedStatement statement = connection.prepareStatement( query, ResultSet.TYPE_FORWARD_ONLY, resultSetConcur);
    if ( stream) statement.setFetchSize( Integer.MIN_VALUE);
    
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

  private final static String driverClassName = "org.apache.cassandra.cql.jdbc.CassandraDriver";
  private final static Log log = Log.getLog( CassandraProvider.class);

  private String database;
  private String url;
}
