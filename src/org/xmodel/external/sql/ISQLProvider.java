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
package org.xmodel.external.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.xmodel.IModelObject;
import org.xmodel.external.CachingException;

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
  public void configure( IModelObject annotation) throws CachingException;
  
  /**
   * Returns a Connection possibly from a pool of Connection objects.
   * @return Returns a Connection possibly from a pool of Connection objects.
   */
  public Connection getConnection() throws CachingException;
  
  /**
   * Returns a PreparedStatement built from the specified SQL. This method is provided to allow
   * the implementation to optionally cache PreparedStatement instances.
   * @param sql The SQL statement.
   * @return Returns a PreparedStatement built from the specified SQL.
   */
  public PreparedStatement prepareStatement( String sql) throws CachingException; 

  /**
   * Returns a PreparedStatement built from the specified SQL. This method is provided to allow
   * the implementation to optionally cache PreparedStatement instances.
   * @param sql The SQL statement.
   * @param type The result set type.
   * @param concurrency The result set concurrency.
   * @param holdability The result set holdability.
   * @return Returns a PreparedStatement built from the specified SQL.
   */
  public PreparedStatement prepareStatement( String sql, int type, int concurrency, int holdability) throws CachingException; 
}
