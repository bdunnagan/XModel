package org.xmodel.caching.sql.transform;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class stores the column types for the columns of a table, and is designed to be reused
 * by multiple instances of SQLCachingPolicy to minimize the number of meta-data requests made to 
 * the database.
 */
public class SQLColumnMetaData
{
  public SQLColumnMetaData()
  {
    types = new ConcurrentHashMap<String, Integer>();
  }
  
  /**
   * Set the column types from the specified source.
   * @param source The meta-data source.
   */
  public void setColumnTypes( ResultSetMetaData source) throws SQLException
  {
    for( int i=1; i<=source.getColumnCount(); i++)
      setColumnType( source.getColumnName( i), source.getColumnType( i));
  }
  
  /**
   * Set the column type for the specified column.
   * @param columnName The name of the column.
   * @param columnType The column type constant.
   */
  public void setColumnType( String columnName, int columnType)
  {
    types.put( columnName, columnType);
  }

  /**
   * Returns the column type constant for the specified column.  If the column type has
   * not been retrieved from the database, then -1 is returned.
   * @param columnName The name of the column.
   * @return Returns -1 or the column type constant for the specified table and column.
   */
  public int getColumnType( String columnName)
  {
    Integer type = types.get( columnName);
    return (type != null)? (int)type: -1;
  }
  
  private Map<String, Integer> types;
}
