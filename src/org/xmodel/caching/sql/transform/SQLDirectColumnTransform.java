package org.xmodel.caching.sql.transform;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.xmodel.IModelObject;

/**
 * An ISQLColumnTransform that stores data in an attribute or child value of the row element.  The only transformation
 * that is performed is that a Java null is stored by calling <code>PreparedStatement.setNull</code> for portability.
 */
public class SQLDirectColumnTransform implements ISQLColumnTransform
{
  /**
   * Create a transform between the attribute or child node with the specified name and the specified table column.
   * @param metadata The table metadata.
   * @param columnName The name of the table column.
   * @param nodeName The name of an attribute or child node.
   * @param attribute True if attribute node should be used.
   */
  public SQLDirectColumnTransform( SQLColumnMetaData metadata, String columnName, String nodeName, boolean attribute)
  {
    this.metadata = metadata;
    this.columnName = columnName;
    this.nodeName = nodeName;
    this.isAttribute = attribute;
  }

  /* (non-Javadoc)
   * @see org.xmodel.caching.sql.transform.ISQLColumnTransform#getColumnName()
   */
  @Override
  public String getColumnName()
  {
    return columnName;
  }

  /* (non-Javadoc)
   * @see org.xmodel.caching.sql.transform.ISQLColumnTransform#importColumn(java.sql.ResultSet, org.xmodel.IModelObject, int)
   */
  @Override
  public void importColumn( ResultSet cursor, IModelObject rowElement, int columnIndex) throws SQLException
  {
    Object object = cursor.getObject( columnIndex);
    if ( isAttribute)
    {
      rowElement.setAttribute( nodeName, object);
    }
    else
    {
      rowElement.getCreateChild( nodeName).setValue( object);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.caching.sql.transform.ISQLColumnTransform#exportColumn(java.sql.PreparedStatement, org.xmodel.IModelObject, int)
   */
  @Override
  public void exportColumn( PreparedStatement statement, IModelObject rowElement, int columnIndex) throws SQLException
  {
    Object object = isAttribute? rowElement.getAttribute( nodeName): rowElement.getCreateChild( nodeName).getValue();
    if ( object != null) 
    {
      statement.setObject( columnIndex, object); 
    }
    else 
    {
      statement.setNull( columnIndex, metadata.getColumnType( columnName));
    }
  }

  private SQLColumnMetaData metadata;
  private String nodeName;
  private boolean isAttribute;
  private String columnName;
}
