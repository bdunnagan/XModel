package org.xmodel.caching.sql.transform;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmodel.IModelObject;
import org.xmodel.ModelObject;

public class DefaultSQLRowTransform implements ISQLRowTransform
{
  public DefaultSQLRowTransform( String tableName)
  {
    this.tableName = tableName;
    this.rowElementName = tableName;
    this.transformMap = new HashMap<String, ISQLColumnTransform>();
    this.transformList = new ArrayList<ISQLColumnTransform>();
  }

  /**
   * Set the primary key for the table - required for updating.
   * @param primaryKey The primary key.
   */
  public void setPrimaryKey( String primaryKey)
  {
    this.primaryKey = primaryKey;
  }
  
  /**
   * Define a column.
   * @param columnName The name of the column.
   * @param transform The column transform.
   */
  public void defineColumn( String columnName, ISQLColumnTransform transform)
  {
    transformMap.put( columnName, transform);
    transformList.add( transform);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.caching.sql.transform.ISQLRowTransform#importRow(java.sql.ResultSet)
   */
  @Override
  public IModelObject importRow( ResultSet rowCursor) throws SQLException
  {
    IModelObject rowElement = new ModelObject( rowElementName);
    
    for( int i=0; i<transformList.size(); )
      transformList.get( i).importColumn( rowCursor, rowElement, ++i);
    
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.caching.sql.transform.ISQLRowTransform#insertRows(java.sql.Connection, java.util.List)
   */
  @Override
  public void insertRows( Connection connection, List<IModelObject> rowElements) throws SQLException
  {
    if ( rowElements.size() == 0 || transformList.size() == 0) return;
    
    StringBuilder sb = new StringBuilder();
    sb.append( "INSERT INTO "); sb.append( tableName);
    sb.append( " VALUES");
    sb.append( "(");

    // build column name list
    for( int i=0; i<transformList.size(); i++) sb.append( "?,");
    sb.setLength( sb.length() - 1);
    sb.append( ")");

    // create statement
    PreparedStatement statement = connection.prepareStatement( sb.toString());
    
    // create batch of updates for each row element being inserted
    for( IModelObject rowElement: rowElements)
    {
      for( int i=0; i<transformList.size(); )
        transformList.get( i).exportColumn( statement, rowElement, ++i);
      
      statement.addBatch();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.caching.sql.transform.ISQLRowTransform#deleteRows(java.sql.Connection, java.util.List)
   */
  @Override
  public void deleteRows( Connection connection, List<IModelObject> rowElements) throws SQLException
  {
    StringBuilder sb = new StringBuilder();
    sb.append( "DELETE FROM "); sb.append( tableName);
    sb.append( " WHERE "); sb.append( primaryKey);
    sb.append( "=?");

    PreparedStatement statement = connection.prepareStatement( sb.toString());
    
    ISQLColumnTransform transform = transformMap.get( primaryKey);
    for( IModelObject rowElement: rowElements)
    {
      transform.exportColumn( statement, rowElement, 1);
      statement.addBatch();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.caching.sql.transform.ISQLRowTransform#updateRow(java.sql.Connection, org.xmodel.IModelObject, java.util.List)
   */
  @Override
  public void updateRow( Connection connection, IModelObject rowElement, List<String> columnNames) throws SQLException
  {
    StringBuilder sb = new StringBuilder();
    sb.append( "UPDATE "); sb.append( tableName);
    sb.append( " SET ");

    for( String columnName: columnNames)
    {
      sb.append( columnName);
      sb.append( "=?,");
    }
    sb.setLength( sb.length() - 1);
    
    sb.append(" WHERE ");
    sb.append( primaryKey);
    sb.append( "=?");
    
    PreparedStatement statement = connection.prepareStatement( sb.toString());
    for( int i=0; i<columnNames.size(); )
    {
      String column = columnNames.get( i);
      ISQLColumnTransform transform = transformMap.get( column);
      transform.exportColumn( statement, rowElement, ++i);
    }
    
    ISQLColumnTransform transform = transformMap.get( primaryKey);
    transform.exportColumn( statement, rowElement, columnNames.size() + 1);
  }

  private String tableName;
  private String rowElementName;
  private String primaryKey;
  private Map<String, ISQLColumnTransform> transformMap;
  private List<ISQLColumnTransform> transformList;
}
