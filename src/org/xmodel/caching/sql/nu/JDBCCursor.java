package org.xmodel.caching.sql.nu;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.xmodel.IModelObject;

public class JDBCCursor implements ISQLCursor
{
  public JDBCCursor( PreparedStatement statement, IModelObject schema)
  {
    this.statement = statement;
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
    if ( resultSet == null) resultSet = statement.executeQuery();
    return resultSet.next()? transform.transformRow( resultSet): null;
  }

  @Override
  public void dispose() throws SQLException
  {
    statement.close();
  }
  
  private PreparedStatement statement;
  private ResultSet resultSet;
  private SchemaTransform transform;
}
