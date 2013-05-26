package org.xmodel.caching.sql.transform;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.xmodel.IModelObject;

/**
 * An interface for transforming the value of row element values into SQL data-types.
 */
public interface ISQLColumnTransform
{
  /**
   * @return Returns the name of the table column.
   */
  public String getColumnName();
  
  /**
   * Import a column from the database.
   * @param cursor The cursor pointing to the current row.
   * @param rowElement The row element.
   * @param columnIndex The one-based column index.
   */
  public void importColumn( ResultSet cursor, IModelObject rowElement, int columnIndex) throws SQLException;
  
  /**
   * Export a column to the database.  The columnIndex argument is always relative to the statement, which will
   * typically only be updating a subset of the columns of a table row.  Therefore, the columnIndex does not
   * uniquely identify a table column, but only identifies the column being updated with a particular statement.
   * @param rowElement The row element.
   * @param columnIndex The one-based column index.
   * @param cursor The cursor pointing to the current row.
   */
  public void exportColumn( PreparedStatement statement, IModelObject rowElement, int columnIndex) throws SQLException;
}
