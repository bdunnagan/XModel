package org.xmodel.caching.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.xmodel.IModelObject;

/**
 * This class provides a configurable transform from the results of a SQL query to one
 * or more elements. This class allows each column in the result-set to be mapped to an
 * attribute or a child.  Unmapped columns are mapped to children with the same name as
 * the column name returned by the SQL statement.
 */
public class SQLTransform
{
  /**
   * Create a SQLTransform from the specified statement.
   * @param statement The query statement.
   */
  public SQLTransform( PreparedStatement statement)
  {
    this.statement = statement;
  }

  /**
   * Create a row element for the specified row.
   * @param index The zero-based index of the row.
   * @param result The ResultSet object pointing to the row.
   * @return Returns the 
   */
  public IModelObject createRow( int index, ResultSet result, ResultSetMetaData meta)
  {
  }
  
  /**
   * Create the representation of the column in the specified parent element.
   * @param index The zero-based index of the column.
   * @param result The ResultSet object point to the current row.
   * @param parent The row element.
   */
  public void createColumn( int index, ResultSet result, IModelObject parent)
  {
  }
  
  /**
   * Execute the query.
   * @return Returns the transformed result-set.
   */
  public List<IModelObject> execute() throws SQLException
  {
    List<IModelObject> rows = new ArrayList<IModelObject>();
    
    ResultSet result = statement.executeQuery();
    while( result.next())
    {
      IModelObject row = createRow( result.getRow() - 1, result);
      for( int i=0; i<columnCount; i++)
      {
        createColumn( i, result, row);
      }
    }    
    
    return rows;
  }

  private PreparedStatement statement;
}
