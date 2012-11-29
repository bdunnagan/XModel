package org.xmodel.caching.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.xmodel.IModelObject;

/**
 * An implementation of ISQLQueryTransform that aims to be as efficient as possible, including
 * avoiding the use of ResultSetMetaData.
 */
public class SQLTransform implements ISQLQueryTransform
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
  public IModelObject createRow( int index, ResultSet result)
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
  
  /* (non-Javadoc)
   * @see org.xmodel.caching.sql.ISQLQueryTransform#transform(java.sql.ResultSet)
   */
  @Override
  public List<IModelObject> transform( ResultSet result) throws SQLException
  {
    List<IModelObject> rows = new ArrayList<IModelObject>();
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
