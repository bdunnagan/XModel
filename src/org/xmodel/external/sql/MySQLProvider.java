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
package org.xmodel.external.sql;

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
  public MySQLProvider()
  {
    pool = new ConnectionPool( this, 10);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.external.caching.SQLCachingPolicy.SQLManager#configure(org.xmodel.IModelObject)
   */
  public void configure( IModelObject annotation) throws CachingException
  {
    String database = Xlate.childGet( annotation, "database", (String)null);
    if ( database == null) throw new CachingException( "Database not defined in annotation: "+annotation);

    String host = Xlate.childGet( annotation, "host", "127.0.0.1");
    url = String.format( "jdbc:mysql://%s/%s", host, database);  
   
    login = Xlate.childGet( annotation, "login", (String)null);
    if ( login == null) throw new CachingException( "Login not defined in annotation: "+annotation);
    
    password = Xlate.childGet( annotation, "password", (String)null);
    if ( password == null) throw new CachingException( "Password not defined in annotation: "+annotation);
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.sql.ISQLProvider#newConnection()
   */
  public Connection newConnection() throws CachingException
  {
    try
    {
      return DriverManager.getConnection( url, login, password);
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
    return pool.lease();
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.sql.ISQLProvider#returnConnection(java.sql.Connection)
   */
  @Override
  public void releaseConnection( Connection connection)
  {
    pool.release( connection);
  }

  //private final static String driverClassName = "com.mysql.jdbc.Driver";
  
  private String url;
  private String login;
  private String password;
  private ConnectionPool pool;
}
