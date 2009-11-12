/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * BlobAccess.java
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

import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Instances of this class populate the values of elements representing columns in a database table row
 * which contain large binary data. The <code>toString</code> is only useful for debugging purposes as
 * it does not produce a true string serialization.
 */
public class BlobAccess 
{
  public BlobAccess( PreparedStatement statement)
  {
    this.statement = statement;
  }
  /**
   * Queries and returns a Blob reference for the associate table row column.
   * @return Returns the Blob reference.
   */
  public Blob getBlob() throws SQLException
  {
    ResultSet result = statement.executeQuery();
    result.first();
    return result.getBlob( 1);
  }
  
  private PreparedStatement statement;
}
