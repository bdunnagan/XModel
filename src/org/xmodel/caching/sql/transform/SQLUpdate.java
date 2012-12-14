package org.xmodel.caching.sql.transform;

import java.io.InputStream;

import org.xmodel.IModelObject;

/**
 * An abstraction from the JDBC statement interfaces that carries additional information 
 * about how the statement is being used so that it can be executed appropriately.  For
 * example, some JDBC drivers require that PreparedStatement.executeBatch() be called 
 * when the <code>addBatch</code> method is used.
 */
public class SQLUpdate
{
  /**
   * Create the SQL to update the specified column in the specified row.  If the object
   * is null, then the table column must be nullable and the NULL will be stored.
   * @param rowElement The row element representing the table row.
   * @param columnName The name of the column to be updated.
   * @param object An SQL data object.
   */
  public void update( IModelObject rowElement, String columnName, Object object)
  {
  }
  
  /**
   * Create the SQL to update the specified column in the specified row.
   * @param rowElement The row element representing the table row.
   * @param columnName The name of the column to be updated.
   * @param stream The stream from which the new data will be read.
   * @param stream An input stream from which to read the new data.
   */
  public void update( IModelObject rowElement, String columnName, InputStream stream)
  {
  }
}
