package org.xmodel.caching.sql.nu;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.xmodel.IModelObject;

public class JDBCCursor implements ISQLCursor
{
  public JDBCCursor( ResultSet resultSet, IModelObject schema)
  {
    this.resultSet = resultSet;
  }
  
  @Override
  public void reset() throws SQLException
  {
    if ( !resultSet.isBeforeFirst())
    {
      resultSet = ((PreparedStatement)resultSet.getStatement()).executeQuery();
    }
  }

  @Override
  public IModelObject next() throws SQLException
  {
    if ( resultSet == null) resultSet = ((PreparedStatement)resultSet.getStatement()).executeQuery();
    return resultSet.next()? transform.transformRow( resultSet): null;
  }

  @Override
  public void dispose() throws SQLException
  {
    resultSet.getStatement().close();
  }
  
  private ResultSet resultSet;
  private SchemaTransform transform;
}
