/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * JDCConnectionDriver.java
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

import java.sql.*;
import java.util.*;

public class JDCConnectionDriver implements Driver
{
  public static final String URL_PREFIX = "jdbc:jdc:";
  private static final int MAJOR_VERSION = 1;
  private static final int MINOR_VERSION = 0;
  private JDCConnectionPool pool;

  public JDCConnectionDriver( String driver, String url, String user, String password) 
  throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException
  {
    DriverManager.registerDriver( this);
    Class.forName( driver).newInstance();
    pool = new JDCConnectionPool( url, user, password);
  }

  public Connection connect( String url, Properties props) throws SQLException 
  {
    if ( !url.startsWith( URL_PREFIX)) return null;
    return pool.getConnection();
  }

  public boolean acceptsURL( String url)
  {
    return url.startsWith( URL_PREFIX);
  }

  public int getMajorVersion()
  {
    return MAJOR_VERSION;
  }

  public int getMinorVersion()
  {
    return MINOR_VERSION;
  }

  public DriverPropertyInfo[] getPropertyInfo( String str, Properties props)
  {
    return new DriverPropertyInfo[0];
  }

  public boolean jdbcCompliant()
  {
    return false;
  }
}