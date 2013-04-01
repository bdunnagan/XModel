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
    String host = Xlate.childGet( annotation, "host", "localhost");
    url = String.format( "jdbc:mysql://%s/", host);  
   
    username = Xlate.childGet( annotation, "username", (String)null);
    if ( username == null) throw new CachingException( "Username not defined in annotation: "+annotation);
    
    password = Xlate.childGet( annotation, "password", (String)null);
    if ( password == null) throw new CachingException( "Password not defined in annotation: "+annotation);
    
    database = Xlate.childGet( annotation, "database", (String)null);
    
    pool = new ConnectionPool( this, 20);
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
   * @see org.xmodel.caching.sql.ISQLProvider#createStatement(java.sql.Connection, java.lang.String, long, long)
   */
  @Override
  public PreparedStatement createStatement( Connection connection, String query, long limit, long offset) throws SQLException
  {
    if ( limit < 0) return connection.prepareStatement( query);
    if ( offset < 0) return connection.prepareStatement( String.format( "%s LIMIT %d", limit, offset));
    return connection.prepareStatement( String.format( "%s LIMIT %d OFFSET %d", limit, offset));
  }

  private final static String driverClassName = "com.mysql.jdbc.Driver";
  private final static Log log = Log.getLog( MySQLProvider.class);
  
  private String url;
  private String username;
  private String password;
  private String database;
  private ConnectionPool pool;
}
