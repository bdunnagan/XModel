package org.xmodel.caching.sql.transform;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.xmodel.IModelObject;

/**
 * An interface that defines the transform of SQL table columns to and from IModelObject instances.
 */
public interface ISQLColumnTransform
{
  /**
   * Import one column from the specified ResultSet.
   * @param rowCursor The ResultSet pointing at the current row.
   * @param rowElement The element representing the current row.
   * @param columnIndex The zero-based index of the column to be transformed.
   */
  public void importColumn( ResultSet rowCursor, IModelObject rowElement, int columnIndex) throws SQLException;
  
  /**
   * Export one column from the specified row element and 
   * @param sql The SQL update builder.
   * @param rowElement The row element.
   * @param columnName The name of the column to be transformed.
   */
  public void exportColumn( SQLUpdate sql, IModelObject rowElement, String columnName) throws SQLException;
}
