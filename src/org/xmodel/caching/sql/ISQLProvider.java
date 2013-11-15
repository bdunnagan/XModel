/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * SQLManager.java
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
import java.sql.SQLException;
import org.xmodel.IModelObject;

/**
 * An interface for creating (and possibly caching) instances of java.sql.Statement. An implementation
 * of the interface must be defined on the <i>meta:sqlmanager</i> attribute or <i>meta:sqlmanager</i> child 
 * of an ancestor of each IExternalReference which uses an SQLCachingPolicy.
 */
public interface ISQLProvider
{
  /**
   * Configure this manager from the specified annotation.
   * @param annotation The annotation.
   */
  public void configure( IModelObject annotation);

  /**
   * @return Returns null or the name of the database defined for this provider.
   */
  public String getDatabase();
  
  /**
   * Create a new Connection instance.
   * @return Returns the new Connection instance.
   */
  public Connection newConnection();
  
  /**
   * Lease a Connection instance from the Connection pool.
   * @return Returns a Connection instance.
   */
  public Connection leaseConnection();
  
  /**
   * Return a Connection instance to the Connection pool.
   * @param connection The connection.
   */
  public void releaseConnection( Connection connection);
  
  /**
   * Create a PreparedStatement for the specified query with a vendor-specific implementation of the limit and offset parameters.
   * @param connection The JDBC connection.
   * @param query The base SQL query.
   * @param limit The maximum number of rows to return, or -1.
   * @param offset The offset of the first row to return, or -1.
   * @param stream True if streaming-mode should be used.
   * @param readonly True if statement is for read-only access.
   * @return Returns a PreparedStatement with the specified query plus vendor-specific implementation of the limit and offset parameters.
   */
  public PreparedStatement createStatement( Connection connection, String query, long limit, long offset, boolean stream, boolean readonly) throws SQLException;
}
