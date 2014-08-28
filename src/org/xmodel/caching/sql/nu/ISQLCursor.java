package org.xmodel.caching.sql.nu;

import java.sql.SQLException;
import org.xmodel.IModelObject;

public interface ISQLCursor
{
  public void reset() throws SQLException;
  
  public IModelObject next() throws SQLException;
  
  public void dispose() throws SQLException;
}
