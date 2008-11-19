/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
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
    return result.getBlob( 0);
  }
  
  private PreparedStatement statement;
}
