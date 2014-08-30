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
package org.xmodel.caching.sql.nu.cassandra;

import java.sql.SQLException;
import org.xmodel.IModelObject;
import org.xmodel.Xlate;
import org.xmodel.caching.sql.nu.ISQLCursor;
import org.xmodel.caching.sql.nu.ISQLProvider;
import org.xmodel.caching.sql.nu.ISQLRequest;
import org.xmodel.external.CachingException;

/**
 * An implementation of ISQLProvider for the Cassandra database.
 */
public class CassandraProvider implements ISQLProvider
{
  public CassandraProvider() throws ClassNotFoundException
  {
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
    
    String database = Xlate.childGet( annotation, "database", (String)null);
    
    String host = Xlate.childGet( annotation, "host", "localhost");
    int port = Xlate.childGet( annotation, "port", 9160);
  }

  @Override
  public ISQLCursor query( ISQLRequest request) throws SQLException
  {
    return null;
  }

  @Override
  public void update( ISQLRequest request) throws SQLException
  {
  }
}
