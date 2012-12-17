package org.xmodel.caching.sql.transform;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.xmodel.IChangeRecord;
import org.xmodel.IModelObject;

/**
 * An interface that defines the transformation between an SQL table row and an IModelObject instance.
 */
public interface ISQLRowTransform
{
  /**
   * Import one row from the specified ResultSet.
   * @param rowCursor A ResultSet positioned at the row to be imported.
   * @return Returns the imported row.
   */
  public IModelObject importRow( ResultSet rowCursor) throws SQLException;
  
  /**
   * Insert rows into a table.
   * @param connection The database connection.
   * @param rowElements The elements containing the data to be inserted.
   * @param valueTransform The value transform.
   */
  public void insertRows( Connection connection, List<IModelObject> rowElements) throws SQLException;
  
  /**
   * Delete rows from a table.
   * @param connection The database connection.
   * @param rowElements The elements to be deleted.
   * @param valueTransform The value transform.
   */
  public void deleteRows( Connection connection, List<IModelObject> rowElements) throws SQLException;
  
  /**
   * Update a row in the table.
   * @param connection The database connection.
   * @param rowElement The row to update.
   * @param record The list of changes made to the row element.
   * @param valueTransform The value transform.
   */
  public void updateRow( Connection connection, IModelObject rowElement, List<IChangeRecord> records) throws SQLException;
}
