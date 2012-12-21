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
import org.xmodel.IModelObject;
import org.xmodel.Xlate;
import org.xmodel.external.CachingException;

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
    
    database = Xlate.childGet( annotation, "database", Xlate.childGet( annotation, "catalog", (String)null));
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
    return newConnection();
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.sql.ISQLProvider#returnConnection(java.sql.Connection)
   */
  @Override
  public void releaseConnection( Connection connection)
  {
    try { connection.close();} catch( Exception e) {}
  }

  private final static String driverClassName = "com.mysql.jdbc.Driver";
  
  private String url;
  private String username;
  private String password;
  private String database;
}
