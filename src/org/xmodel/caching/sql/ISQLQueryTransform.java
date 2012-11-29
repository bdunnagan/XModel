package org.xmodel.caching.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.xmodel.IModelObject;

/**
 * An interface for defining how a SQL query is transformed and stored.
 */
public interface ISQLQueryTransform
{
  /**
   * Transform the specified ResultSet.
   * @param result The result of a SQL query.
   * @return Returns the transformed result.
   */
  public List<IModelObject> transform( ResultSet result) throws SQLException;
}
